package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;


import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author bik
 * @since 2025-12-04
 */
@Data
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单唯一流水号
     */
    @TableField("no")
    private String no;

    @TableField("user_id")
    private Integer userId;

    /**
     * 收货地址
     */
    @TableField("address")
    private String address;

    /**
     * 订单总价格
     */
    @TableField("total_price")
    private BigDecimal totalPrice;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 支付时间
     */
    @TableField("paid_time")
    private Integer paidTime;

    /**
     * 支付方式
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付平台订单号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 退款状态
     */
    @TableField("refund_status")
    private String refundStatus;

    /**
     * 退款单号
     */
    @TableField("refund_no")
    private String refundNo;

    /**
     * 是否关闭
     */
    @TableField("closed")
    private Boolean closed;

    /**
     * 物流状态
     */
    @TableField("ship_status")
    private String shipStatus;

    /**
     * 物流数据
     */
    @TableField("ship_data")
    private String shipData;

    /**
     * 额外数据
     */
    @TableField("extra")
    private String extra;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;

    /**
     * 是否已评价
     */
    @TableField("reviewed")
    private Boolean reviewed;

    /**
     * 使用优惠券id
     */
    @TableField("coupon_user_id")
    private Integer couponUserId;
}
