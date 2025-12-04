package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author bik
 * @since 2025-12-04
 */
@Getter
@Setter
@TableName("coupon_user")
public class CouponUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("coupon_id")
    private Integer couponId;

    @TableField("user_id")
    private Integer userId;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;

    /**
     * 是否已使用 0未使用 1已使用
     */
    @TableField("used")
    private Byte used;
}
