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
 * 发票
 * </p>
 *
 * @author bik
 * @since 2025-12-04
 */
@Getter
@Setter
@TableName("invoice")
public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 名称/公司名称
     */
    @TableField("name")
    private String name;

    /**
     * 手机
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 税号
     */
    @TableField("code")
    private String code;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 单位地址
     */
    @TableField("path")
    private String path;

    /**
     * 开户行
     */
    @TableField("bankname")
    private String bankname;

    /**
     * 银行账号
     */
    @TableField("bankno")
    private String bankno;

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

    /**
     * 订单id
     */
    @TableField("order_id")
    private Integer orderId;

    /**
     * 状态：0未开票1已开票
     */
    @TableField("status")
    private Boolean status;

    /**
     * 类型：0个人1企业
     */
    @TableField("type")
    private Boolean type;
}
