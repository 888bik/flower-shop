package com.bik.flower_shop.controller.user;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.context.BaseController;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.service.CouponService;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/user/coupon")
@RequiredArgsConstructor
public class CouponUserController extends BaseController {

    private final CouponService couponService;

    private final TokenService tokenService;

    @Override
    protected TokenService getTokenService() {
        return tokenService;
    }

    /**
     * 全部可领取优惠券
     * 登录 / 未登录都可以访问
     */
    @GetMapping("/all")
    public ApiResult<Map<String, Object>> listAllCoupons(@RequestHeader(value = "accessToken", required = false) String accessToken) {
        User user = null;
        if (accessToken != null && !accessToken.isBlank()) {
            user = tokenService.getUserByAccessToken(accessToken);
        }
        Map<String, Object> data = couponService.listAllCoupons(user);
        return ApiResult.ok(data);
    }


    /**
     * 我的优惠券（必须登录）
     */
    @AuthRequired
    @GetMapping("/list")
    public ApiResult<Map<String, Object>> listUserCoupons() {
        User user = getCurrentUser();
        Map<String, Object> data = couponService.listUserCouponsWithStatus(user.getId());
        return ApiResult.ok(data);
    }

    /**
     * 领取优惠券（必须登录）
     */
    @PostMapping("/receive/{id}")
    public ApiResult<String> receiveCoupon(@PathVariable("id") Integer couponId) {
        User user = getCurrentUser();
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
