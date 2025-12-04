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
 * 用户提现表
 * </p>
 *
 * @author bik
 * @since 2025-12-04
 */
@Getter
@Setter
@TableName("user_extract")
public class UserExtract implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 提现人ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 真实名称
     */
    @TableField("real_name")
    private String realName;

    /**
     * 提现方式：wx微信, hand手动
     */
    @TableField("extract_type")
    private String extractType;

    /**
     * 微信二维码
     */
    @TableField("wechat")
    private String wechat;

    /**
     * -1未通过,0审核中,1已提现
     */
    @TableField("status")
    private Boolean status;

    /**
     * 申请时间
     */
    @TableField("create_time")
    private Integer createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Integer updateTime;
}
