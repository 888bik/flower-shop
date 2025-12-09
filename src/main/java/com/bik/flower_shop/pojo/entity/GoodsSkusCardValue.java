package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
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
@Data
@TableName("goods_skus_card_value")
public class GoodsSkusCardValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品规格卡片id
     */
    @TableField("goods_skus_card_id")
    private Integer goodsSkusCardId;

    /**
     * 值
     */
    @TableField("name")
    private String name;

    /**
     * 扩展值
     */
    @TableField("value")
    private String value;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;
}
