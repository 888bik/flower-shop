package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class SkusController {
    @GetMapping("/skus/{page}")
    public ApiResult<?> listSkus(@PathVariable int page) { return ApiResult.ok(); }

    @PostMapping("/skus")
    public ApiResult<?> createSkus(@RequestBody Object body) { return ApiResult.ok(); }

    @PostMapping("/skus/delete_all")
    public ApiResult<?> deleteSkus() { return ApiResult.ok(); }

    @PostMapping("/skus/{id}")
    public ApiResult<?> updateSkus(@PathVariable Long id, @RequestBody Object body) { return ApiResult.ok(); }

    @PostMapping("/skus/{id}/update_status")
    public ApiResult<?> updateSkusStatus(@PathVariable Long id) { return ApiResult.ok(); }
}
