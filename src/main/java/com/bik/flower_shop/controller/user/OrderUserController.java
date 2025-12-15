package com.bik.flower_shop.controller.user;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.OrderCreateDTO;
import com.bik.flower_shop.pojo.entity.Order;
import com.bik.flower_shop.service.OrderService;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单接口
 * @author bik
 */
@RestController
@RequestMapping("/user/orders")
@RequiredArgsConstructor
public class OrderUserController {

    private final OrderService orderService;
    private final TokenService tokenService;

    @PostMapping
    public ApiResult<Order> create(@RequestHeader("token") String token,
                                   @Validated @RequestBody OrderCreateDTO dto) {
        var user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        Order o = orderService.createOrder(user.getId(), dto);
        return ApiResult.ok(o);
    }

    @GetMapping
    public ApiResult<List<Order>> list(@RequestHeader("token") String token,
                                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                                       @RequestParam(value = "limit", defaultValue = "12") Integer limit) {
        var user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        List<Order> list = orderService.listByUser(user.getId(), page, limit);
        return ApiResult.ok(list);
    }

    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> detail(@RequestHeader("token") String token,
                                                 @PathVariable Integer id) {
        var user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        Map<String, Object> detail = orderService.getOrderDetail(user.getId(), id);
        return ApiResult.ok(detail);
    }

    @PostMapping("/{id}/cancel")
    public ApiResult<?> cancel(@RequestHeader("token") String token, @PathVariable Integer id) {
        var user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        orderService.cancelOrder(user.getId(), id);
        return ApiResult.ok();
    }

    @PostMapping("/{id}/pay")
    public ApiResult<?> pay(@RequestHeader("token") String token,
                            @PathVariable Integer id,
                            @RequestParam String method,
                            @RequestParam(required = false) String paymentNo) {
        var user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        orderService.payOrder(user.getId(), id, method, paymentNo);
        return ApiResult.ok();
    }
}
