package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.ImageMapper;
import com.bik.flower_shop.pojo.entity.Image;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author bik
 */
@Slf4j
@Service
public class ImageService {

    @Autowired
    private ImageMapper imageMapper;

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

    public Map<String, Object> listByClassId(Integer classId, int page, int limit) {
        int offset = (page - 1) * limit;
        List<Image> list = imageMapper.selectByClassIdPage(classId, limit, offset);
        int totalCount = imageMapper.countByClassId(classId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        return result;
    }

    /**
     * 上传图片到腾讯云并保存数据库
     *
     * @param imageClassId 相册ID
     * @param files        图片文件数组
     * @return 上传后的 Image 列表
     */
    public List<Image> uploadImages(Integer imageClassId, MultipartFile[] files) throws Exception {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        long maxBytes = maxFileSizeMB * 1024L * 1024L;
        long compressBytes = compressThresholdMB * 1024L * 1024L;

        List<Image> uploaded = new ArrayList<>();

        COSCredentials cred = new BasicCOSCredentials(tencentSecretId, tencentSecretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(tencentRegion));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                if (file.getSize() > maxBytes) {
                    throw new BusinessException("文件过大，最大允许 " + maxFileSizeMB + "MB");
                }

                String originalFilename = Objects.requireNonNull(file.getOriginalFilename()).replaceAll("\\s+", "_");
                String objectName = System.currentTimeMillis() + "_" +
                        UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;

                // === 写入临时文件 ===
                File tmpFile = createTempFile(file, originalFilename);

                File fileToUpload = tmpFile;

                // === 压缩判断 ===
                if (compressBytes > 0 && tmpFile.length() > compressBytes) {
                    File compressed = compressImage(tmpFile, originalFilename);

                    if (compressed != null && compressed.exists() && compressed.length() < tmpFile.length()) {
                        fileToUpload = compressed;
                    } else {
                        if (compressed != null) compressed.delete();
                    }
                }

                // === 设置 metadata ===
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(fileToUpload.length());

                PutObjectRequest putObjectRequest =
                        new PutObjectRequest(tencentBucketName, objectName, fileToUpload);
                putObjectRequest.setMetadata(metadata);

                cosClient.putObject(putObjectRequest);

                // === 保存数据库 ===
                Image img = new Image();
                img.setImageClassId(imageClassId);
                img.setName(originalFilename);
                img.setPath(objectName);
                img.setUrl("https://" + tencentBucketName + ".cos." + tencentRegion + ".myqcloud.com/" + objectName);
                img.setCreateTime((int) (System.currentTimeMillis() / 1000));
                img.setUpdateTime((int) (System.currentTimeMillis() / 1000));

                imageMapper.insert(img);
                uploaded.add(img);

                // === 清理临时文件 ===
                tmpFile.delete();
                if (fileToUpload != tmpFile) {
                    fileToUpload.delete();
                }
            }
        } finally {
            cosClient.shutdown();
        }

        return uploaded;
    }

    /**
     * 将 MultipartFile 写入临时文件
     */
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

    /**
     * 压缩图片
     */
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
            log.error("图片压缩失败:", e);
            return null;
        }
    }

//    private void deleteQuietly(File f) {
//        try {
//            if (f != null && f.exists()) {
//                f.delete();
//            }
//        } catch (Exception ignored) {
//        }
//    }

    public boolean deleteImagesByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // 查询图片路径
        List<Image> images = imageMapper.selectList(new QueryWrapper<Image>().in("id", ids));

        COSCredentials cred = new BasicCOSCredentials(tencentSecretId, tencentSecretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(tencentRegion));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            for (Image image : images) {
                String objectName = image.getPath();
                if (objectName != null && !objectName.isEmpty()) {
                    try {
                        cosClient.deleteObject(tencentBucketName, objectName);
                    } catch (Exception ex) {
                        // 记录错误，但继续删除其它对象（可根据需要改为回滚）
                        ex.printStackTrace();
                    }
                }
            }
        } finally {
            cosClient.shutdown();
        }

        // 删除数据库记录
        return imageMapper.delete(new QueryWrapper<Image>().in("id", ids)) > 0;
    }

    // 根据分类ID统计图片数量
    public Long countByClassId(Integer classId) {
        return imageMapper.selectCount(new QueryWrapper<Image>().eq("image_class_id", classId));
    }

    /**
     * 修改图片名称
     */
    public boolean updateImageName(Integer imageId, String newName) {
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException("图片不存在");
        }

        image.setName(newName);
        int updated = imageMapper.updateById(image);
        return updated > 0;
    }

}
