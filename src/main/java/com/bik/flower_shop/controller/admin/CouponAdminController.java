package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.pojo.dto.CouponAdminDTO;
import com.bik.flower_shop.pojo.dto.StatusDTO;
import com.bik.flower_shop.pojo.entity.Coupon;
import com.bik.flower_shop.service.CouponService;
import com.bik.flower_shop.common.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@AuthRequired(role = "admin")
@RequestMapping("/admin/coupon")
public class CouponAdminController {

    private final CouponService couponService;

    /**
     * 新增优惠券
     */
    @PostMapping
    public ApiResult<Coupon> createCoupon(@RequestBody CouponAdminDTO dto) {
        Coupon coupon = couponService.createCoupon(dto);
        return ApiResult.ok(coupon);
    }

    /**
     * 修改优惠券
     */
    @PostMapping("/{id}")
    public ApiResult<Boolean> updateCoupon(@PathVariable Integer id, @RequestBody CouponAdminDTO dto) {
        boolean success = couponService.updateCoupon(id, dto);
        return ApiResult.ok(success);
    }

    /**
     * 删除优惠券
     */
    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> deleteCoupon(@PathVariable Integer id) {
        boolean success = couponService.deleteCoupon(id);
        return ApiResult.ok(success);
    }

    /**
     * 获取优惠券列表
     */
    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> getCouponList(@PathVariable int page,
                                               @RequestParam(required = false, defaultValue = "10") int limit) {
        Map<String, Object> result = couponService.listCoupons(page, limit);
        return ApiResult.ok(result);
    }

    /**
     * 修改优惠券状态
     */
    @PostMapping("/{id}/update_status")
    public ApiResult<Boolean> updateCouponStatus(@PathVariable Integer id,
                                           @RequestBody StatusDTO dto) {
        boolean success = couponService.updateCouponStatus(id, dto.getStatus());
        return ApiResult.ok(success);
    }

}
