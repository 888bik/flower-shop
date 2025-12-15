package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.dto.UserCouponDTO;
import com.bik.flower_shop.pojo.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    @Select("SELECT * FROM coupon ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Coupon> selectPage(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM coupon")
    Integer countAll();

    // 查询所有可用优惠券 + 当前用户领取状态
    List<UserCouponDTO> selectUserCoupons(Integer userId);

    @Select("""
                SELECT
                    c.*,
                    CASE WHEN #{userId} IS NOT NULL AND uc.id IS NOT NULL THEN TRUE ELSE FALSE END AS received,
                    (c.total - IFNULL(u.count, 0)) AS stock
                FROM coupon c
                LEFT JOIN (
                    SELECT coupon_id, COUNT(*) AS count
                    FROM coupon_user
                    GROUP BY coupon_id
                ) u ON c.id = u.coupon_id
                LEFT JOIN coupon_user uc 
                    ON c.id = uc.coupon_id 
                    AND uc.user_id = #{userId}
                WHERE c.status = 1
                  AND c.start_time <= #{now}
                  AND c.end_time >= #{now}
                  AND (c.total - c.used) > 0
                ORDER BY c.`order` DESC, c.create_time DESC
            """)
    List<UserCouponDTO> selectAvailableCouponsForUser(@Param("now") Integer now,
                                                      @Param("userId") Integer userId);

}
