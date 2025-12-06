package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.entity.Category;
import com.bik.flower_shop.pojo.vo.CategoryTreeVO;
import com.bik.flower_shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    /**
     * 获取分类列表
     */
    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> getCategoriesList(@PathVariable int page,
                                                            @RequestParam(required = false, defaultValue = "10") int limit) {
        return ApiResult.ok(categoryService.listCategories(page, limit));
    }

    /**
     * 创建分类
     */
    @PostMapping
    public ApiResult<Category> createCategory(@RequestBody Category dto) {
        return ApiResult.ok(categoryService.createCategory(dto));
    }

    /**
     * 修改分类
     */
    @PostMapping("/{id}")
    public ApiResult<Boolean> updateCategory(@PathVariable Integer id, @RequestBody Category dto) {
        return ApiResult.ok(categoryService.updateCategory(id, dto));
    }

    /**
     * 删除分类
     */
    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> deleteCategory(@PathVariable Integer id) {
        return ApiResult.ok(categoryService.deleteCategory(id));
    }

    /**
     * 修改分类状态
     */
    @PostMapping("/{id}/update_status")
    public ApiResult<Boolean> updateCategoryStatus(@PathVariable Integer id,
                                                   @RequestBody Map<String, Byte> body) {
        Byte status = body.get("status");
        return ApiResult.ok(categoryService.updateCategoryStatus(id, status));
    }

}
