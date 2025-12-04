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
@TableName("user_level")
public class UserLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 等级名称
     */
    @TableField("name")
    private String name;

    /**
     * 等级权重
     */
    @TableField("level")
    private Integer level;

    /**
     * 状态：0禁用1启用
     */
    @TableField("status")
    private Boolean status;

    /**
     * 折扣
     */
    @TableField("discount")
    private Integer discount;

    /**
     * 消费满元
     */
    @TableField("max_price")
    private Integer maxPrice;

    /**
     * 消费满次数
     */
    @TableField("max_times")
    private Integer maxTimes;
}
