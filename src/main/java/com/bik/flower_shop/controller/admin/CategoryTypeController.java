package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.mapper.CategoryTypeMapper;
import com.bik.flower_shop.pojo.entity.CategoryType;
import com.bik.flower_shop.pojo.vo.CategoryTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@RestController
@RequestMapping("/admin/category_type")
@RequiredArgsConstructor
public class CategoryTypeController {
    private final CategoryTypeMapper typeMapper;

    /**
     * 创建分类类型
     */
    @PostMapping
    public ApiResult<CategoryType> createType(@RequestBody CategoryType t) {
        t.setCreateTime((int) (System.currentTimeMillis() / 1000));
        t.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        typeMapper.insert(t);
        return ApiResult.ok(t);
    }

    /**
     * 更新分类类型
     */
    @PutMapping("/{id}")
    public ApiResult<CategoryType> updateType(@PathVariable Integer id, @RequestBody CategoryType t) {
        CategoryType existing = typeMapper.selectById(id);
        if (existing == null) {
            return ApiResult.fail("分类类型不存在");
        }
        existing.setName(t.getName());
        existing.setCode(t.getCode());
        existing.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        typeMapper.updateById(existing);
        return ApiResult.ok(existing);
    }

    /**
     * 删除分类类型
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteType(@PathVariable Integer id) {
        CategoryType existing = typeMapper.selectById(id);
        if (existing == null) {
            return ApiResult.fail("分类类型不存在");
        }
        typeMapper.deleteById(id);
        return ApiResult.ok();
    }

    @GetMapping("/list")
    public ApiResult<List<CategoryType>> list() {
        List<CategoryType> list = typeMapper.selectList(null);
        return ApiResult.ok(list);
    }

    /**
     * 获取所有分类类型（VO，前端需要 code + name）
     */
    @GetMapping("/list/vo")
    public ApiResult<List<CategoryTypeVO>> listVO() {
        List<CategoryType> list = typeMapper.selectList(null);
        List<CategoryTypeVO> voList = list.stream().map(t -> {
            CategoryTypeVO vo = new CategoryTypeVO();
            vo.setCode(t.getCode());   // 前端英文标识
            vo.setName(t.getName());   // 中文显示
            return vo;
        }).collect(Collectors.toList());
        return ApiResult.ok(voList);
    }

}
