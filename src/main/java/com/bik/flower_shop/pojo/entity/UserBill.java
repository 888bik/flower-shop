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
 * 用户分佣账单表
 * </p>
 *
 * @author bik
 * @since 2025-12-04
 */
@Getter
@Setter
@TableName("user_bill")
public class UserBill implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 获取佣金用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 等级：1一级佣金，2二级佣金
     */
    @TableField("level")
    private Boolean level;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Integer orderId;

    /**
     * 佣金
     */
    @TableField("commission")
    private BigDecimal commission;

    /**
     * 状态 0冻结中，1生效，-1失效
     */
    @TableField("status")
    private Boolean status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Integer createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Integer updateTime;
}
