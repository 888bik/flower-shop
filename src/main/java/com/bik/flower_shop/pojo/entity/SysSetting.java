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
@TableName("sys_setting")
public class SysSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 开启注册，0关闭1开启
     */
    @TableField("open_reg")
    private Boolean openReg;

    /**
     * 注册方式，username普通phone手机
     */
    @TableField("reg_method")
    private String regMethod;

    /**
     * 密码最小长度
     */
    @TableField("password_min")
    private Integer passwordMin;

    /**
     * 强制密码复杂度
     */
    @TableField("password_encrypt")
    private String passwordEncrypt;

    /**
     * 上传方式
     */
    @TableField("upload_method")
    private String uploadMethod;

    /**
     * 上传配置
     */
    @TableField("upload_config")
    private String uploadConfig;

    /**
     * 是否开启API安全，0否1是
     */
    @TableField("api_safe")
    private Boolean apiSafe;

    /**
     * api秘钥
     */
    @TableField("api_secret")
    private String apiSecret;

    /**
     * 自动取消订单，分钟
     */
    @TableField("close_order_minute")
    private Integer closeOrderMinute;

    /**
     * 自动收货时间，天
     */
    @TableField("auto_received_day")
    private Integer autoReceivedDay;

    /**
     * 允许申请售后，天
     */
    @TableField("after_sale_day")
    private Integer afterSaleDay;

    /**
     * 支付宝支付配置
     */
    @TableField("alipay")
    private String alipay;

    /**
     * 微信支付配置
     */
    @TableField("wxpay")
    private String wxpay;

    /**
     * 物流相关配置
     */
    @TableField("ship")
    private String ship;
}
