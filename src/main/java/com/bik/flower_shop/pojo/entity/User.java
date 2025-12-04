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
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("avatar")
    private String avatar;

    @TableField("nickname")
    private String nickname;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("user_level_id")
    private Integer userLevelId;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;

    @TableField("last_login_time")
    private Integer lastLoginTime;

    /**
     * 状态：0禁用1启用
     */
    @TableField("status")
    private Boolean status;

    /**
     * 微信openid
     */
    @TableField("wechat_openid")
    private String wechatOpenid;

    /**
     *  推广用户数量
     */
    @TableField("share_num")
    private Integer shareNum;

    /**
     * 推广订单数量
     */
    @TableField("share_order_num")
    private Integer shareOrderNum;

    /**
     * 订单金额
     */
    @TableField("order_price")
    private BigDecimal orderPrice;

    /**
     * 账户佣金
     */
    @TableField("commission")
    private BigDecimal commission;

    /**
     * 已提现金额
     */
    @TableField("cash_out_price")
    private BigDecimal cashOutPrice;

    /**
     * 已提现次数
     */
    @TableField("cash_out_time")
    private Integer cashOutTime;

    /**
     * 未提现金额
     */
    @TableField("no_cash_out_price")
    private BigDecimal noCashOutPrice;

    /**
     * 一级推广人ID
     */
    @TableField("p1")
    private Integer p1;

    /**
     * 二级推广人ID
     */
    @TableField("p2")
    private Integer p2;
}
