package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.CategoryMapper;
import com.bik.flower_shop.mapper.CategoryTypeMapper;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.pojo.entity.Category;
import com.bik.flower_shop.pojo.entity.CategoryType;
import com.bik.flower_shop.pojo.vo.CategoryTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final GoodsMapper goodsMapper;
    private final CategoryTypeMapper typeMapper;

    public Map<String, Object> listCategories(int page, int limit) {

        QueryWrapper<Category> query = new QueryWrapper<>();
        query.orderByAsc("`order`").orderByDesc("create_time");

        // 总数
        Long total = categoryMapper.selectCount(query);

        // 分页
        int offset = (page - 1) * limit;
        query.last("LIMIT " + offset + "," + limit);

        List<Category> list = categoryMapper.selectList(query);

        Map<String, Object> res = new HashMap<>();
        res.put("list", list);
        res.put("totalCount", total);
        return res;
    }



    private void sortRecursively(List<CategoryTreeVO> list, Comparator<CategoryTreeVO> comp) {
        list.sort(comp);
        for (CategoryTreeVO vo : list) {
            if (vo.getChildren() != null) {
                sortRecursively(vo.getChildren(), comp);
            }
        }
    }

    private void ensureNotNull(List<CategoryTreeVO> list) {
        for (CategoryTreeVO vo : list) {
            if (vo.getChildren() == null) {
                vo.setChildren(new ArrayList<>());
            } else {
                ensureNotNull(vo.getChildren());
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public Category createCategory(Category dto) {
        if (dto.getOrder() == null) {
            dto.setOrder(0);
        }
        int now = (int) Instant.now().getEpochSecond();
        dto.setCreateTime(now);
        dto.setUpdateTime(now);
        categoryMapper.insert(dto);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(Integer id, Category dto) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("分类不存在", 400);
        }
        exist.setName(dto.getName());
        exist.setStatus(dto.getStatus());
        exist.setCategoryId(dto.getCategoryId());
        exist.setOrder(dto.getOrder() == null ? 0 : dto.getOrder());
        exist.setUpdateTime((int) Instant.now().getEpochSecond());
        return categoryMapper.updateById(exist) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Integer id) {
        // 判断是否有商品关联
        int count = goodsMapper.selectCount(new QueryWrapper<com.bik.flower_shop.pojo.entity.Goods>().eq("category_id", id)).intValue();
        if (count > 0) {
            throw new BusinessException("该分类下存在商品，不能删除", 400);
        }
        // 判断是否有子分类
        int childCount = categoryMapper.selectCount(new QueryWrapper<Category>().eq("category_id", id)).intValue();
        if (childCount > 0) {
            throw new BusinessException("该分类下存在子分类，不能删除", 400);
        }
        return categoryMapper.deleteById(id) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategoryStatus(Integer id, Byte status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        return categoryMapper.updateById(category) > 0;
    }
}
