package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.pojo.dto.CouponDTO;
import com.bik.flower_shop.pojo.entity.Coupon;
import com.bik.flower_shop.service.CouponService;
import com.bik.flower_shop.common.ApiResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/admin/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // 新增优惠券
    @PostMapping
    public ApiResult<Coupon> createCoupon(@ModelAttribute CouponDTO dto) {
        Coupon coupon = couponService.createCoupon(dto);
        return ApiResult.ok(coupon);
    }

    // 修改优惠券
    @PostMapping("/{id}")
    public ApiResult<Boolean> updateCoupon(@PathVariable Integer id, @ModelAttribute CouponDTO dto) {
        boolean success = couponService.updateCoupon(id, dto);
        return ApiResult.ok(success);
    }

    // 删除优惠券
    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> delete(@PathVariable Integer id) {
        boolean success = couponService.deleteCoupon(id);
        return ApiResult.ok(success);
    }

    // 优惠券列表
    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> list(@PathVariable int page,
                                               @RequestParam(required = false, defaultValue = "10") int limit) {
        Map<String, Object> result = couponService.listCoupons(page, limit);
        return ApiResult.ok(result);
    }

    @PostMapping("/{id}/update_status")
    public ApiResult<Boolean> updateStatus(@PathVariable Integer id,
                                           @RequestParam("status") Byte status) {
        boolean success = couponService.updateCouponStatus(id, status);
        return ApiResult.ok(success);
    }


}
