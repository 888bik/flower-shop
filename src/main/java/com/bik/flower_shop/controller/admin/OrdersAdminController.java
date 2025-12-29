package com.bik.flower_shop.controller.admin;

import com.alibaba.fastjson.JSON;
import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.*;
import com.bik.flower_shop.pojo.entity.Orders;
import com.bik.flower_shop.pojo.vo.OrderAdminPageVO;
import com.bik.flower_shop.service.OrdersAdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
@AuthRequired(role = "admin")
public class OrdersAdminController {


    private final OrdersAdminService ordersAdminService;

    /**
     * 批量删除订单
     */
    @PostMapping("/delete_all")
    public ApiResult<Integer> deleteOrdersBulk(@RequestBody Map<String, List<Integer>> body) {
        List<Integer> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ApiResult.fail("ids 不能为空");
        }
        int deleted = ordersAdminService.deleteOrdersBulk(ids);
        return ApiResult.ok(deleted);
    }

    /**
     * 获取订单列表
     */
    @GetMapping
    public ApiResult<OrderAdminPageVO> listOrders(@ModelAttribute OrderListQueryDTO dto) throws JsonProcessingException {
        OrderAdminPageVO page = ordersAdminService.listOrders(dto);
        return ApiResult.ok(page);
    }

    /**
     * 发货
     */
    @PostMapping("/{id}/ship")
    public ApiResult<?> shipOrder(@PathVariable("id") Long id,
                                  @RequestBody @Validated ShipOrderRequest req) throws JsonProcessingException {
        ordersAdminService.shipOrder(id, req);
        return ApiResult.ok("发货成功");
    }

    /**
     * 导出订单
     */
    @PostMapping("/excelexport")
    public ApiResult<?> exportOrders() {
        // 这里可以返回文件下载地址或直接流（此处示例占位）
        // 实际可使用 Apache POI 生成 Excel 并返回下载
        ordersAdminService.exportOrders();
        return ApiResult.ok(0);
    }

    /**
     * 查询订单物流信息
     */
    @GetMapping("/{orderId}/ship")
    public ApiResult<ShipDataDTO> getShipData(@PathVariable Long orderId) {
        Orders order = ordersAdminService.getOrderById(orderId);
        if (order == null) {
            return ApiResult.fail("订单不存在");
        }

        String shipDataStr = order.getShipData();
        if (shipDataStr == null || shipDataStr.isEmpty()) {
            return ApiResult.ok(null);
        }

        ShipDataDTO shipData = JSON.parseObject(shipDataStr, ShipDataDTO.class);
        return ApiResult.ok(shipData);
    }

    /**
     * 订单退款处理
     */
    @PostMapping("/refund/handle")
    public ApiResult<Void> handleRefund(@RequestBody RefundHandleDTO dto) {
        ordersAdminService.handleRefund(dto.getOrderId(), Boolean.TRUE.equals(dto.getAgree()), dto.getReason(), dto.getRefundType());
        return ApiResult.ok();
    }

    /**
     * 订单退款确认
     */
    @PostMapping("/refund/confirm")
    public ApiResult<Void> confirmRefund(@RequestBody ConfirmRefundRequest request) {
        ordersAdminService.confirmRefund(request.getOrderId());
        return ApiResult.ok();
    }


    /**
     * 拒绝用户退货退款
     */
    @PostMapping("/refund/return/reject")
    public ApiResult<Void> rejectReturn(@RequestBody RejectReturnRequest request) {
        ordersAdminService.rejectReturn(request.getOrderId(), request.getReason());
        return ApiResult.ok();
    }

}
