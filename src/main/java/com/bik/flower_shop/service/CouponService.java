package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bik.flower_shop.mapper.CouponMapper;
import com.bik.flower_shop.mapper.CouponUserMapper;
import com.bik.flower_shop.pojo.dto.CouponAdminDTO;
import com.bik.flower_shop.pojo.dto.UserCouponDTO;
import com.bik.flower_shop.pojo.entity.Coupon;
import com.bik.flower_shop.pojo.entity.CouponUser;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponMapper couponMapper;
    private final CouponUserMapper couponUserMapper;
    private final TokenService tokenService;

    // 新增优惠券
    public Coupon createCoupon(CouponAdminDTO dto) {
        Coupon coupon = new Coupon();
        coupon.setName(dto.getName());
        coupon.setType(dto.getType());
        coupon.setValue(dto.getValue());
        coupon.setTotal(dto.getTotal());
        coupon.setUsed(0);
        coupon.setMinPrice(dto.getMinPrice());
        coupon.setStartTime(dto.getStartTime());
        coupon.setEndTime(dto.getEndTime());
        coupon.setStatus((byte) 1);
        int now = (int) Instant.now().getEpochSecond();
        coupon.setCreateTime(now);
        coupon.setUpdateTime(now);
        coupon.setDescription(dto.getDescription());
        coupon.setSort(Objects.requireNonNullElse(dto.getSort(), 50));

        couponMapper.insert(coupon);
        return coupon;
    }

    // 修改优惠券
    public boolean updateCoupon(Integer id, CouponAdminDTO dto) {
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
        if (dto.getSort() != null) {
            coupon.setSort(dto.getSort());
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


    // 获取用户优惠券（可用 / 已用 / 过期）
    public Map<String, Object> listUserCouponsWithStatus(Integer userId) {

        List<UserCouponDTO> all = couponMapper.selectUserCoupons(userId);
        long now = Instant.now().getEpochSecond();

        // 可用：未使用 + 在有效期内
        List<UserCouponDTO> valid = all.stream()
                .filter(c ->
                        c.getUsed() != null && c.getUsed() == 0
                                && (c.getStartTime() == null || c.getStartTime() <= now)
                                && (c.getEndTime() == null || c.getEndTime() > now)
                )
                .toList();

        // 已过期（时间过期）
        List<UserCouponDTO> expired = all.stream()
                .filter(c ->
                        c.getEndTime() != null && c.getEndTime() <= now
                )
                .toList();

        // 已使用
        List<UserCouponDTO> used = all.stream()
                .filter(c ->
                        c.getUsed() != null && c.getUsed() == 1
                )
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("list", valid);                // 前端下单用
        result.put("used", used);                 // 已使用
        result.put("expired", expired);           // 已过期
        result.put("totalCount", valid.size());

        return result;
    }


    // 获取所有优惠券列表
    public Map<String, Object> listAllCoupons(User user) {
        Integer userId = (user != null) ? user.getId() : null;

        int now = (int) (System.currentTimeMillis() / 1000);
        List<UserCouponDTO> coupons = couponMapper.selectAvailableCouponsForUser(now, userId);

        // 转换 DTO，主要处理时间格式
        List<UserCouponDTO> couponDtos = coupons.stream().peek(c -> {
            c.setTime(TimeUtils.formatDate(c.getStartTime()) + " ～ " + TimeUtils.formatDate(c.getEndTime()));
            c.setScope("全场鲜花通用");
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("list", couponDtos);
        result.put("totalCount", couponDtos.size());

        return result;
    }


    // 用户领取优惠券
    @Transactional(rollbackFor = Exception.class)
    public void receiveCoupon(Integer userId, Integer couponId) throws IllegalArgumentException {
        // 1) 检查是否已领取
        Long count = couponUserMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CouponUser>()
                        .eq(CouponUser::getCouponId, couponId)
                        .eq(CouponUser::getUserId, userId)
        );

        if (count != null && count > 0) {
            throw new IllegalArgumentException("已领取过该优惠券");
        }

        // 2) 查询优惠券最新状态
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new IllegalArgumentException("优惠券不存在或已失效");
        }

        int stock = (coupon.getTotal() == null ? 0 : coupon.getTotal()) - (coupon.getUsed() == null ? 0 : coupon.getUsed());
        if (stock <= 0) {
            throw new IllegalArgumentException("优惠券已领完");
        }

        // 3) 乐观更新 coupon.used = used + 1 (并发安全：WHERE used < total)
        int affected = couponMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Coupon>()
                        .lambda()
                        .eq(Coupon::getId, couponId)
                        .setSql("used = used + 1")
                        .lt(Coupon::getUsed, coupon.getTotal())
        );
        if (affected <= 0) {
            // 并发下已被抢完
            throw new IllegalArgumentException("优惠券已被抢光");
        }

        // 4) 插入 coupon_user 记录
        CouponUser cu = new CouponUser();
        cu.setCouponId(couponId);
        cu.setUserId(userId);
        int now = (int) Instant.now().getEpochSecond();
        cu.setCreateTime(now);
        cu.setUpdateTime(now);
        cu.setUsed((byte) 0);
        couponUserMapper.insert(cu);
    }
}
