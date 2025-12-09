package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.ImageMapper;
import com.bik.flower_shop.pojo.entity.Image;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
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
        List<Image> uploadedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            String objectName = System.currentTimeMillis() + "_" + originalFilename;

            // 上传到腾讯云
            String url = uploadTencent(file.getBytes(), objectName);

            // 构建 Image 对象
            Image image = new Image();
            image.setImageClassId(imageClassId);
            image.setName(originalFilename);
            image.setPath(objectName);
            image.setUrl(url);
            image.setCreateTime((int) (System.currentTimeMillis() / 1000));
            image.setUpdateTime((int) (System.currentTimeMillis() / 1000));

            // 保存数据库
            imageMapper.insert(image);

            uploadedImages.add(image);
        }

        return uploadedImages;
    }

    /**
     * 上传到腾讯云
     */
    private String uploadTencent(byte[] bytes, String objectName) {
        COSCredentials cred = new BasicCOSCredentials(tencentSecretId, tencentSecretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(tencentRegion));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    tencentBucketName,
                    objectName,
                    new ByteArrayInputStream(bytes),
                    null
            );
            cosClient.putObject(putObjectRequest);

            return "https://" + tencentBucketName + ".cos." + tencentRegion + ".myqcloud.com/" + objectName;
        } finally {
            cosClient.shutdown();
        }
    }

    public boolean deleteImagesByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // 查询图片路径
        List<Image> images = imageMapper.selectList(new QueryWrapper<Image>().in("id", ids));

        // 删除腾讯 COS 上的文件
        COSCredentials cred = new BasicCOSCredentials(tencentSecretId, tencentSecretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(tencentRegion));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            for (Image image : images) {
                String objectName = image.getPath();
                if (objectName != null && !objectName.isEmpty()) {
                    cosClient.deleteObject(tencentBucketName, objectName);
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
