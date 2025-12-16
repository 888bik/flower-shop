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
@TableName("coupon")
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 优惠券名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型：固定金额/百分比折扣   	0固定金额 1百分比
     */
    @TableField("type")
    private Byte type;

    /**
     * 折扣值，根据不同类型含义不同
     */
    @TableField("value")
    private BigDecimal value;

    /**
     * 总数
     */
    @TableField("total")
    private Integer total;

    /**
     * 已使用
     */
    @TableField("used")
    private Integer used;

    /**
     * 最低价格
     */
    @TableField("min_price")
    private BigDecimal minPrice;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private Integer startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private Integer endTime;

    /**
     * 优惠券是否生效 0不生效 1生效
     */
    @TableField("status")
    private Byte status;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 备注
     */
    @TableField(value = "`desc`")
    private String description;
}
