package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.CategoryMapper;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.pojo.entity.Category;
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

    public Map<String, Object> listCategories(int page, int limit) {

        List<Category> all = categoryMapper.selectList(null);

        // 转为 VO
        List<CategoryTreeVO> voList = all.stream().map(c -> {
            CategoryTreeVO vo = new CategoryTreeVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setStatus(c.getStatus());
            vo.setOrder(c.getOrder());
            vo.setCategoryId(c.getCategoryId());
            vo.setCreateTime(c.getCreateTime());
            vo.setUpdateTime(c.getUpdateTime());
            return vo;
        }).toList();

        // 建立 Map
        Map<Integer, CategoryTreeVO> map = voList.stream()
                .collect(Collectors.toMap(CategoryTreeVO::getId, v -> v));

        // 构建树
        List<CategoryTreeVO> roots = new ArrayList<>();
        for (CategoryTreeVO vo : voList) {
            Integer parentId = vo.getCategoryId();
            if (parentId == null || parentId == 0) {
                roots.add(vo);
            } else {
                CategoryTreeVO parent = map.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                    parent.getChildren().add(vo);
                }
            }
        }
        // 排序
        Comparator<CategoryTreeVO> comp = Comparator.comparing(
                CategoryTreeVO::getOrder,
                Comparator.nullsLast(Integer::compareTo)
        );
        sortRecursively(roots, comp);

        // 再分页（只分页一级分类 roots）
        int total = roots.size();
        int from = (page - 1) * limit;
        int to = Math.min(from + limit, total);
        List<CategoryTreeVO> pageList = from >= total ? Collections.emptyList() : roots.subList(from, to);

        // children 永不为 null
        ensureNotNull(pageList);

        Map<String, Object> res = new HashMap<>();
        res.put("list", pageList);
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
