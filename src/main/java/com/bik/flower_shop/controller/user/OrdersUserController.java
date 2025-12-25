package com.bik.flower_shop.controller.user;

import com.alibaba.fastjson.JSON;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.context.BaseController;
import com.bik.flower_shop.pojo.dto.*;
import com.bik.flower_shop.pojo.entity.Orders;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.pojo.vo.OrderDetailVO;
import com.bik.flower_shop.pojo.vo.OrderListResponse;
import com.bik.flower_shop.pojo.vo.ReviewItemVO;
import com.bik.flower_shop.service.OrdersUserService;
import com.bik.flower_shop.service.TokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单接口
 *
 * @author bik
 */
@RestController
@RequestMapping("/user/orders")
@RequiredArgsConstructor
public class OrdersUserController extends BaseController {

    private final OrdersUserService ordersUserService;
    private final TokenService tokenService;

    @Override
    protected TokenService getTokenService() {
        return tokenService;
    }


    /**
     * 创建订单
     */
    @PostMapping("/create")
    public ApiResult<Map<String, Object>> createOrder(
            @RequestBody @Valid OrderCreateDTO dto) throws JsonProcessingException {

        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        Map<String, Object> res = ordersUserService.createOrder(user.getId(), dto);
        return ApiResult.ok(res);
    }

    /**
     * 获取订单列表
     */
    @GetMapping
    public ApiResult<OrderListResponse> list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                             @RequestParam(value = "limit", defaultValue = "10") Integer limit) throws JsonProcessingException {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        OrderListResponse list = ordersUserService.listOrdersByUser(user.getId(), page, limit);
        return ApiResult.ok(list);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public ApiResult<OrderDetailVO> detail(@PathVariable Integer id) throws JsonProcessingException {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        OrderDetailVO detail = ordersUserService.getOrderDetail(user.getId(), id);
        return ApiResult.ok(detail);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public ApiResult<?> cancel(@PathVariable Integer id) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        ordersUserService.cancelOrder(user.getId(), id);
        return ApiResult.ok();
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    public ApiResult<?> pay(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payMethod) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        ordersUserService.payOrder(user.getId(), id, payMethod.get("payMethod"));
        return ApiResult.ok();
    }

    /**
     * 查看订单物流
     */
    @GetMapping("/{orderId}/ship")
    public ApiResult<ShipDataDTO> getUserShipData(@PathVariable Long orderId) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        // 2. 查询订单
        Orders order = ordersUserService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals(user.getId())) {
            return ApiResult.fail("订单不存在或无权限查看");
        }

        String shipDataStr = order.getShipData();
        if (shipDataStr == null || shipDataStr.isEmpty()) {
            return ApiResult.ok(null);
        }

        ShipDataDTO shipData = JSON.parseObject(shipDataStr, ShipDataDTO.class);
        return ApiResult.ok(shipData);
    }

    /**
     * 用户确认收货
     */
    @PostMapping("/{id}/receive")
    public ApiResult<?> confirmReceive(@PathVariable Integer id) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        ordersUserService.confirmReceive(user.getId(), id);
        return ApiResult.ok("确认收货成功");
    }

    /**
     * 获取订单下待评价的商品列表
     */
    @GetMapping("/{orderId}/review/items")
    public ApiResult<List<ReviewItemVO>> getReviewItems(
            @PathVariable Integer orderId) {

        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        List<ReviewItemVO> items = ordersUserService.getReviewItems(user.getId(), orderId);
        return ApiResult.ok(items);
    }


    /**
     * 提交评价：一次提交多个商品的评价
     */
    @PostMapping("/{orderId}/review")
    public ApiResult<?> submitReview(@PathVariable Integer orderId,
                                     @RequestBody ReviewSubmitDTO dto) {

        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        ordersUserService.submitReview(user.getId(), orderId, dto);
        return ApiResult.ok("评价提交成功");
    }

    /**
     * 用户批量删除订单（软删除）
     * 前端传入 { "ids": [1, 2, 3] }
     */
    @PostMapping("/delete")
    public ApiResult<Integer> deleteOrders(
            @RequestBody Map<String, List<Integer>> body) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        List<Integer> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ApiResult.fail("ids 不能为空");
        }

        int deleted = ordersUserService.markDeletedByUser(user.getId(), ids);
        return ApiResult.ok(deleted);
    }

    /**
     * 用户申请退款
     */
    @PostMapping("/refund/apply")
    public ApiResult<Void> applyRefund(@RequestBody RefundApplyDTO dto) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        ordersUserService.applyRefund(user.getId(), dto.getOrderId(), dto.getReason(), dto.getRefundType());
        return ApiResult.ok();
    }

    /**
     * 用户提交退货
     */
    @PostMapping("/refund/return")
    public ApiResult<Void> submitReturn(@RequestBody RefundReturnDTO dto) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        ordersUserService.submitReturn(dto, user.getId());
        return ApiResult.ok();
    }

    /**
     * 用户提交退货物流信息
     */
    @PostMapping("/return/ship")
    public ApiResult<Void> submitReturnShip(@RequestBody @Validated RefundReturnShipDTO dto) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        ordersUserService.submitReturnShip(user.getId(), dto);
        return ApiResult.ok();
    }

}
