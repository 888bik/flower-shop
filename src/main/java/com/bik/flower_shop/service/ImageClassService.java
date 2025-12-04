package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.mapper.ImageClassMapper;
import com.bik.flower_shop.pojo.entity.ImageClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageClassService {
    private final ImageClassMapper imageClassMapper;

    public List<ImageClass> listAllWithImageCount() {
        List<ImageClass> list = imageClassMapper.selectList(null);
        for (ImageClass c : list) {
            int count = imageClassMapper.countImagesByClassId(c.getId());
            c.setImagesCount(count);
        }
        return list;
    }

    public ImageClass createImageClass(ImageClass imageClass) {
        // 如果order为空，给默认值
        if (imageClass.getOrder() == null) {
            imageClass.setOrder(0);
        }
        imageClassMapper.insert(imageClass);
        return imageClass;
    }

    public boolean updateImageClassById(Integer id, String name, Integer order) {
        ImageClass imageClass = new ImageClass();
        imageClass.setId(id);
        imageClass.setName(name);
        imageClass.setOrder(order);

        int updated = imageClassMapper.updateById(imageClass);
        return updated > 0;
    }

    public boolean deleteById(Integer id) {
        return imageClassMapper.deleteById(id) > 0;
    }

}
