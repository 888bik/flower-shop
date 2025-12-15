package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

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
@TableName("order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单id
     */
    @TableField("order_id")
    private Integer orderId;


    /**
     * 购买数量
     */
    @TableField("num")
    private Integer num;

    /**
     * 购买价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 评分
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 评价
     */
    @TableField("review")
    private String review;

    /**
     * 评价时间
     */
    @TableField("review_time")
    private Integer reviewTime;

    @TableField("create_time")
    private Integer createTime;

    /**
     * 规格类型
     */
    @TableField("skus_type")
    private Byte skusType;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField("goods_num")
    private Integer goodsNum;

    @TableField("user_id")
    private Integer userId;

    /**
     * 客服回复评论
     */
    @TableField("extra")
    private String extra;

    /**
     * 是否可用,1：可用，0不可用
     */
    @TableField("status")
    private Boolean status;
}
