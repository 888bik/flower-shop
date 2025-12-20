package com.bik.flower_shop.controller.user;

import com.alibaba.fastjson.JSON;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.mapper.OrdersMapper;
import com.bik.flower_shop.pojo.dto.OrderCreateDTO;
import com.bik.flower_shop.pojo.dto.ReviewSubmitDTO;
import com.bik.flower_shop.pojo.dto.ShipDataDTO;
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
import org.springframework.transaction.annotation.Transactional;
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
public class OrdersUserController {

    private final OrdersUserService ordersService;
    private final TokenService tokenService;


    /**
     * 创建订单
     */
    @PostMapping("/create")
    public ApiResult<Map<String, Object>> createOrder(
            @RequestHeader("token") String token,
            @RequestBody @Valid OrderCreateDTO dto) throws JsonProcessingException {

        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        Map<String, Object> res = ordersService.createOrder(user.getId(), dto);
        return ApiResult.ok(res);
    }

    /**
     * 获取订单列表
     */
    @GetMapping
    public ApiResult<OrderListResponse> list(@RequestHeader("token") String token,
                                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                                             @RequestParam(value = "limit", defaultValue = "10") Integer limit) throws JsonProcessingException {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        OrderListResponse list = ordersService.listOrdersByUser(user.getId(), page, limit);
        return ApiResult.ok(list);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public ApiResult<OrderDetailVO> detail(@RequestHeader("token") String token,
                                           @PathVariable Integer id) throws JsonProcessingException {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        OrderDetailVO detail = ordersService.getOrderDetail(user.getId(), id);
        return ApiResult.ok(detail);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public ApiResult<?> cancel(@RequestHeader("token") String token, @PathVariable Integer id) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        ordersService.cancelOrder(user.getId(), id);
        return ApiResult.ok();
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    public ApiResult<?> pay(@RequestHeader("token") String token,
                            @PathVariable Integer id,
                            @RequestBody Map<String, String> payMethod) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        ordersService.payOrder(user.getId(), id, payMethod.get("payMethod"));
        return ApiResult.ok();
    }

    /**
     * 查看订单物流
     */
    @GetMapping("/{orderId}/ship")
    public ApiResult<ShipDataDTO> getUserShipData(@RequestHeader("token") String token, @PathVariable Long orderId) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        // 2. 查询订单
        Orders order = ordersService.getOrderById(orderId);
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
    public ApiResult<?> confirmReceive(@RequestHeader("token") String token, @PathVariable Integer id) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        ordersService.confirmReceive(user.getId(), id);
        return ApiResult.ok("确认收货成功");
    }

    /**
     * 获取订单下待评价的商品列表
     */
    @GetMapping("/{orderId}/review/items")
    public ApiResult<List<ReviewItemVO>> getReviewItems(
            @RequestHeader("token") String token,
            @PathVariable Integer orderId) {

        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        List<ReviewItemVO> items = ordersService.getReviewItems(user.getId(), orderId);
        return ApiResult.ok(items);
    }


    /**
     * 提交评价：一次提交多个商品的评价
     */
    @PostMapping("/{orderId}/review")
    public ApiResult<?> submitReview(
            @RequestHeader("token") String token,
            @PathVariable Integer orderId,
            @RequestBody ReviewSubmitDTO dto) {

        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        ordersService.submitReview(user.getId(), orderId, dto);
        return ApiResult.ok("评价提交成功");
    }

    /**
     * 用户批量删除订单（软删除）
     * 前端传入 { "ids": [1, 2, 3] }
     */
    @PostMapping("/delete")
    public ApiResult<Integer> deleteOrders(@RequestHeader("token") String token,
                                           @RequestBody Map<String, List<Integer>> body) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        List<Integer> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ApiResult.fail("ids 不能为空");
        }

        int deleted = ordersService.markDeletedByUser(user.getId(), ids);
        return ApiResult.ok(deleted);
    }

}
