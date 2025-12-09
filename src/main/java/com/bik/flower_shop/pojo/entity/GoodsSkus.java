package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
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
@Data
@TableName("goods_skus")
public class GoodsSkus implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("image")
    private String image;

    @TableField("pprice")
    private BigDecimal pprice;

    @TableField("oprice")
    private BigDecimal oprice;

    @TableField("cprice")
    private BigDecimal cprice;

    /**
     * 库存
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 体积
     */
    @TableField("volume")
    private BigDecimal volume;

    /**
     * 体重
     */
    @TableField("weight")
    private BigDecimal weight;

    /**
     * 编码
     */
    @TableField("code")
    private String code;

    @TableField("goods_id")
    private Integer goodsId;

    /**
     * 对应sku的id组合
     */
    @TableField("skus")
    private String skus;
}
