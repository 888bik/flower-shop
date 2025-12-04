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
@TableName("express_value")
public class ExpressValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 配送模板id
     */
    @TableField("express_id")
    private Integer expressId;

    /**
     * 可配送区域
     */
    @TableField("region")
    private String region;

    /**
     * 首件(个)/首重(Kg)
     */
    @TableField("first")
    private Double first;

    /**
     * 运费(元)
     */
    @TableField("first_price")
    private BigDecimal firstPrice;

    /**
     * 续件/续重
     */
    @TableField("add")
    private Double add;

    /**
     * 续费(元)
     */
    @TableField("add_price")
    private BigDecimal addPrice;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;
}
