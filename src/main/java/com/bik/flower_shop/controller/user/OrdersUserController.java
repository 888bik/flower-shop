package com.bik.flower_shop.controller.user;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.OrderCreateDTO;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.pojo.vo.OrderDetailVO;
import com.bik.flower_shop.pojo.vo.OrderListResponse;
import com.bik.flower_shop.service.OrdersUserService;
import com.bik.flower_shop.service.TokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/cancel")
    public ApiResult<?> cancel(@RequestHeader("token") String token, @PathVariable Integer id) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        ordersService.cancelOrder(user.getId(), id);
        return ApiResult.ok();
    }

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


}
