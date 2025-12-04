package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class OrderController {
    @PostMapping("/order/delete_all")
    public ApiResult<?> deleteOrdersBulk() {
        return ApiResult.ok();
    }

    @GetMapping("/order/{page}")
    public ApiResult<?> listOrders(@PathVariable int page) {
        return ApiResult.ok();
    }

    @PostMapping("/order/{id}/ship")
    public ApiResult<?> shipOrder(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/order/{id}/handle_refund")
    public ApiResult<?> handleRefund(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @PostMapping("/order/excelexport")
    public ApiResult<?> exportOrders() {
        return ApiResult.ok();
    }

    @GetMapping("/order/{id}/get_ship_info")
    public ApiResult<?> getShipInfo(@PathVariable Long id) {
        return ApiResult.ok();
    }
}
