package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.ImageClassMapper;
import com.bik.flower_shop.pojo.entity.ImageClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class ImageClassService {

    private final ImageClassMapper imageClassMapper;
    private final ImageService imageService;

    public Map<String, Object> listAllWithImageCount(int page, int limit) {
        Page<ImageClass> p = new Page<>(page, limit);
        Page<ImageClass> result = imageClassMapper.selectPage(
                p,
                new QueryWrapper<ImageClass>().orderByDesc("id")
        );

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("totalCount", result.getTotal());
        return map;
    }

    public ImageClass createImageClass(ImageClass imageClass) {
        // 如果order为空，给默认值
        if (imageClass.getOrder() == null) {
            imageClass.setOrder(0);
        }
        imageClassMapper.insert(imageClass);
        return imageClass;
    }

    @Transactional
    public boolean updateImageClassById(Integer id, String name, Integer order) {
        ImageClass imageClass = new ImageClass();
        imageClass.setId(id);
        imageClass.setName(name);
        imageClass.setOrder(order);

        int updated = imageClassMapper.updateById(imageClass);
        return updated > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteImageClassById(Integer id) {
        // 查询该分类下图片数量
        Long imageCount = imageService.countByClassId(id);
        System.out.println("imageCount: " + imageCount);
        if (imageCount > 0) {
            // 如果有图片，直接抛异常
            throw new BusinessException("该图库分类下还有图片，不能删除！");
        }
        // 否则删除
        return imageClassMapper.deleteById(id) > 0;
    }
}

