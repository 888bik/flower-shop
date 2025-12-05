package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class CategoryController {
    @GetMapping("/category")
    public ApiResult<?> listCategories() {
        return ApiResult.ok();
    }

    @PostMapping("/category")
    public ApiResult<?> createCategory(@RequestBody Object body) {
        return ApiResult.ok();
    }

    @PostMapping("/category/sort")
    public ApiResult<?> sortCategory(@RequestBody Object body) {
        return ApiResult.ok();
    }

    @PostMapping("/category/{id}/update_status")
    public ApiResult<?> updateCategoryStatus(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/category/{id}/delete")
    public ApiResult<?> deleteCategory(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/category/{id}")
    public ApiResult<?> updateCategory(@PathVariable Long id, @RequestBody Object body) {
        return ApiResult.ok();
    }
}
