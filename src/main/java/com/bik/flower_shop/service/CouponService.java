package com.bik.flower_shop.service;

import com.bik.flower_shop.mapper.CouponMapper;
import com.bik.flower_shop.pojo.dto.CouponDTO;
import com.bik.flower_shop.pojo.entity.Coupon;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CouponService {

    private final CouponMapper couponMapper;

    public CouponService(CouponMapper couponMapper) {
        this.couponMapper = couponMapper;
    }

    // 新增优惠券
    public Coupon createCoupon(CouponDTO dto) {
        Coupon coupon = new Coupon();
        coupon.setName(dto.getName());
        coupon.setType(dto.getType());
        coupon.setValue(dto.getValue());
        coupon.setTotal(dto.getTotal());
        coupon.setUsed(0);
        coupon.setMinPrice(dto.getMinPrice());
        coupon.setStartTime(dto.getStartTime());
        coupon.setEndTime(dto.getEndTime());
        coupon.setStatus((byte) 1); // 默认生效
        int now = (int) Instant.now().getEpochSecond();
        coupon.setCreateTime(now);
        coupon.setUpdateTime(now);
        coupon.setDescription(dto.getDescription());
        coupon.setOrder(Objects.requireNonNullElse(dto.getOrder(), 50));

        couponMapper.insert(coupon);
        return coupon;
    }

    // 修改优惠券
    public boolean updateCoupon(Integer id, CouponDTO dto) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            return false;
        }

        coupon.setName(dto.getName());
        coupon.setType(dto.getType());
        coupon.setValue(dto.getValue());
        coupon.setTotal(dto.getTotal());
        coupon.setMinPrice(dto.getMinPrice());
        coupon.setStartTime(dto.getStartTime());
        coupon.setEndTime(dto.getEndTime());
        coupon.setUpdateTime((int) Instant.now().getEpochSecond());
        if (dto.getOrder() != null) {
            coupon.setOrder(dto.getOrder());
        }
        coupon.setDescription(dto.getDescription());

        return couponMapper.updateById(coupon) > 0;
    }

    // 删除优惠券
    public boolean deleteCoupon(Integer id) {
        return couponMapper.deleteById(id) > 0;
    }

    // 分页列表
    public Map<String, Object> listCoupons(int page, int limit) {
        int offset = (page - 1) * limit;
        List<Coupon> list = couponMapper.selectPage(limit, offset);
        int totalCount = couponMapper.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        return result;
    }

    public boolean updateCouponStatus(Integer id, Byte status) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            return false;
        }

        coupon.setStatus(status);
        coupon.setUpdateTime((int) Instant.now().getEpochSecond());

        return couponMapper.updateById(coupon) > 0;
    }


}
