package com.bik.flower_shop.controller.user;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.UserCouponDTO;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.service.CouponService;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/user/coupon")
@RequiredArgsConstructor
public class CouponUserController {

    private final CouponService couponService;
    private final TokenService tokenService;

    @GetMapping("/all")
    public ApiResult<Map<String, Object>> listAllCoupons(@RequestHeader(value= "token", required = false) String token) {
        Map<String, Object> data = couponService.listAllCoupons(token);
        return ApiResult.ok(data);
    }


    @GetMapping("/list")
    public ApiResult<Map<String, Object>> listUserCoupons(@RequestHeader("token") String token) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        Map<String, Object> data = couponService.listUserCouponsWithStatus(user.getId());
        return ApiResult.ok(data);
    }

    @PostMapping("/receive/{id}")
    public ApiResult<String> receiveCoupon(@RequestHeader("token") String token, @PathVariable("id") Integer couponId) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }

        try {
            couponService.receiveCoupon(user.getId(), couponId);
            return ApiResult.ok("领取成功");
        } catch (IllegalArgumentException e) {
            return ApiResult.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResult.fail("领取失败");
        }
    }


}
