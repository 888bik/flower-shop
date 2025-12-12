package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/order")
@AuthRequired(role = "admin")
public class OrderController {
    @PostMapping("/delete_all")
    public ApiResult<?> deleteOrdersBulk() {
        return ApiResult.ok();
    }

    @GetMapping("/{page}")
    public ApiResult<?> listOrders(@PathVariable int page) {
        return ApiResult.ok();
    }

    @PostMapping("/{id}/ship")
    public ApiResult<?> shipOrder(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/{id}/handle_refund")
    public ApiResult<?> handleRefund(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/excelexport")
    public ApiResult<?> exportOrders() {
        return ApiResult.ok();
    }

    @GetMapping("/{id}/get_ship_info")
    public ApiResult<?> getShipInfo(@PathVariable Long id) {
        return ApiResult.ok();
    }
}
