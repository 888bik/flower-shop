package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    @Select("SELECT * FROM coupon ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Coupon> selectPage(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM coupon")
    Integer countAll();
}
