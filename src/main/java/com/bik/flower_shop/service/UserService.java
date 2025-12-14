package com.bik.flower_shop.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.*;
import com.bik.flower_shop.pojo.dto.RegisterDTO;
import com.bik.flower_shop.pojo.dto.UpdateUserDTO;
import com.bik.flower_shop.pojo.entity.*;
import com.bik.flower_shop.utils.PasswordUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final UserInfoMapper userInfoMapper;
    private final UserAddressesMapper userAddressesMapper;
    private final UserLevelMapper userLevelMapper;
    private final UserBillMapper userBillMapper;
    @Value("${tencent.cos.secretId}")
    private String tencentSecretId;

    @Value("${tencent.cos.secretKey}")
    private String tencentSecretKey;

    @Value("${tencent.cos.bucketName}")
    private String tencentBucketName;

    @Value("${tencent.cos.region}")
    private String tencentRegion;

    // 可配置：最大允许上传大小（MB）
    @Value("${tencent.cos.maxFileSizeMB:50}")
    private long maxFileSizeMB;

    // 可配置：当文件超过此大小（MB）时尝试进行压缩（若不需要压缩可设置为0）
    @Value("${tencent.cos.compressThresholdMB:5}")
    private long compressThresholdMB;

    // 可配置：临时目录
    @Value("${tencent.cos.tmpDir:/tmp}")
    private String tmpDir;

    /**
     * 用户登录
     */
    public String login(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return tokenService.createToken(user, "user");
    }

    public void logout(String token) {
        //通过 token 拿到当前用户
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return; // token 已失效，直接返回（幂等）
        }
        //记录最后在线时间
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime((int) (System.currentTimeMillis() / 1000));

        userMapper.updateById(update);
        // 删除 Redis token
        tokenService.invalidateToken(token, "user");
    }

    /**
     * 用户注册
     */
    public String register(RegisterDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty() ||
                dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new BusinessException("用户名或密码不能为空");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        String username = dto.getUsername().trim();

        long count = userMapper.selectCount(
                new QueryWrapper<User>().eq("username", username)
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail().trim());
        }

        userMapper.insert(user);

        return tokenService.createToken(user, "user");
    }

    public void updateUserInfo(Integer userId, UpdateUserDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        user.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        userMapper.updateById(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserProfile(Integer userId) {
        if (userId == null) {
            return Collections.emptyMap();
        }

        // 1. 基础用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Collections.emptyMap();
        }
        // 去除敏感字段
        user.setPassword(null);

        // 2. 扩展信息
        UserInfo info = userInfoMapper.selectOne(
                new QueryWrapper<UserInfo>().eq("user_id", userId)
        );

        // 3. 地址列表
        List<UserAddresses> addresses = Optional.ofNullable(
                userAddressesMapper.selectList(
                        new QueryWrapper<UserAddresses>()
                                .eq("user_id", userId)
                                .orderByDesc("last_used_time")
                )
        ).orElse(Collections.emptyList());

        // 4. 等级信息
        UserLevel level = null;
        Map<String, Object> levelInfo = null;
        if (user.getUserLevelId() != null) {
            level = userLevelMapper.selectById(user.getUserLevelId());
            if (level != null) {
                levelInfo = new HashMap<>();
                levelInfo.put("id", level.getId());
                levelInfo.put("name", level.getName());
                levelInfo.put("level", level.getLevel());
                levelInfo.put("discount", level.getDiscount());
                levelInfo.put("maxPrice", level.getMaxPrice());
                levelInfo.put("maxTimes", level.getMaxTimes());
                ;
            }
        }

        // 5. 最近分佣账单
        List<UserBill> bills = Optional.ofNullable(
                userBillMapper.selectList(
                        new QueryWrapper<UserBill>()
                                .eq("user_id", userId)
                                .orderByDesc("create_time")
                                .last("limit 20")
                )
        ).orElse(Collections.emptyList());

        // 6. 统计数字
        Map<String, Object> stats = new HashMap<>();
        stats.put("orderPrice", user.getOrderPrice() != null ? user.getOrderPrice().toString() : "0.00");
        stats.put("commission", user.getCommission() != null ? user.getCommission().toString() : "0.00");
        stats.put("cashOutPrice", user.getCashOutPrice() != null ? user.getCashOutPrice().toString() : "0.00");
        stats.put("noCashOutPrice", user.getNoCashOutPrice() != null ? user.getNoCashOutPrice().toString() : "0.00");
        stats.put("shareNum", user.getShareNum());
        stats.put("shareOrderNum", user.getShareOrderNum());

        // 7. 组装返回
        Map<String, Object> res = new HashMap<>();
        res.put("user", user);
        res.put("info", info);
        res.put("addresses", addresses);
        res.put("level", levelInfo);
        res.put("bills", bills);
        res.put("stats", stats);

        return res;
    }


    /**
     * 上传用户头像
     */
    public String uploadAvatar(User user, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        long maxBytes = maxFileSizeMB * 1024L * 1024L;
        long compressBytes = compressThresholdMB * 1024L * 1024L;

        if (file.getSize() > maxBytes) {
            throw new BusinessException("文件过大，最大允许 " + maxFileSizeMB + "MB");
        }

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename())
                .replaceAll("\\s+", "_");
        String objectName = System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;

        // 写入临时文件
        File tmpFile = createTempFile(file, originalFilename);
        File fileToUpload = tmpFile;

        // 压缩大文件
        if (compressBytes > 0 && tmpFile.length() > compressBytes) {
            File compressed = compressImage(tmpFile, originalFilename);
            if (compressed != null && compressed.exists() && compressed.length() < tmpFile.length()) {
                fileToUpload = compressed;
            } else if (compressed != null) {
                compressed.delete();
            }
        }

        // 上传 COS
        String avatarUrl = uploadToCOS(fileToUpload, objectName, file.getContentType());

        // 删除临时文件
        tmpFile.delete();
        if (fileToUpload != tmpFile) {
            fileToUpload.delete();
        }

        // 更新用户表
        user.setAvatar(avatarUrl);
        user.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        userMapper.updateById(user);

        return avatarUrl;
    }

    private File createTempFile(MultipartFile file, String name) throws IOException {
        File dir = new File(tmpDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String suffix = "";
        if (name.contains(".")) {
            suffix = name.substring(name.lastIndexOf('.'));
        }
        File tmp = File.createTempFile("upload_", suffix, dir);
        file.transferTo(tmp);
        return tmp;
    }

    private File compressImage(File src, String name) {
        try {
            File dir = new File(tmpDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String suffix = "";
            if (name.contains(".")) {
                suffix = name.substring(name.lastIndexOf('.'));
            }

            File out = File.createTempFile("compressed_", suffix, dir);
            Thumbnails.of(src)
                    .size(1600, 1600)
                    .outputQuality(0.85)
                    .toFile(out);

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String uploadToCOS(File file, String objectName, String contentType) {
        COSCredentials cred = new BasicCOSCredentials(tencentSecretId, tencentSecretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(tencentRegion));
        COSClient cosClient = new COSClient(cred, clientConfig);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(file.length());

        PutObjectRequest putObjectRequest =
                new PutObjectRequest(tencentBucketName, objectName, file);
        putObjectRequest.setMetadata(metadata);

        cosClient.putObject(putObjectRequest);
        cosClient.shutdown();

        return "https://" + tencentBucketName + ".cos." + tencentRegion + ".myqcloud.com/" + objectName;
    }

}
