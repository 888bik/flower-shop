package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.CategoryTypeMapper;
import com.bik.flower_shop.pojo.entity.CategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class CategoryTypeService {

    private final CategoryTypeMapper typeMapper;

    public List<CategoryType> listAll() {
        return typeMapper.selectList(null);
    }

    public CategoryType getByCode(String code) {
        if (code == null) return null;
        return typeMapper.selectOne(new QueryWrapper<CategoryType>().eq("code", code));
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoryType create(CategoryType dto) {
        int now = (int) Instant.now().getEpochSecond();
        dto.setCreateTime(now);
        dto.setUpdateTime(now);
        typeMapper.insert(dto);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean update(Integer id, CategoryType dto) {
        CategoryType exist = typeMapper.selectById(id);
        if (exist == null) throw new BusinessException("分类类型不存在", 400);
        exist.setCode(dto.getCode());
        exist.setName(dto.getName());
        exist.setStatus(dto.getStatus());
        exist.setOrder(dto.getOrder() == null ? 0 : dto.getOrder());
        exist.setUpdateTime((int) Instant.now().getEpochSecond());
        return typeMapper.updateById(exist) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        // 可在此加检查：某些分类是否仍引用此 type
        return typeMapper.deleteById(id) > 0;
    }
}
