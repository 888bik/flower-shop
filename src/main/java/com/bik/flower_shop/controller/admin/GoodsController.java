package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class GoodsController {
    @GetMapping("/goods/{page}")
    public ApiResult<?> listGoods(@PathVariable int page) {
        return ApiResult.ok();
    }

    @GetMapping("/goods/read/{id}")
    public ApiResult<?> readGoods(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/goods/updateskus/{id}")
    public ApiResult<?> updateGoodsSkus(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/goods/banners/{id}")
    public ApiResult<?> updateBanners(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/goods/restore")
    public ApiResult<?> restoreGoods() {
        return ApiResult.ok();
    }

    @PostMapping("/goods/destroy")
    public ApiResult<?> destroyGoods() {
        return ApiResult.ok();
    }

    @PostMapping("/goods/delete_all")
    public ApiResult<?> deleteGoods() {
        return ApiResult.ok();
    }

    @PostMapping("/goods/changestatus")
    public ApiResult<?> changeGoodsStatus() {
        return ApiResult.ok();
    }

    @PostMapping("/goods")
    public ApiResult<?> createGoods() {
        return ApiResult.ok();
    }

    @PostMapping("/goods/{id}")
    public ApiResult<?> updateGoods(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/goods/{id}/check")
    public ApiResult<?> checkGoods(@PathVariable Long id) {
        return ApiResult.ok();
    }
}
