package com.bik.flower_shop.pojo.entity;// package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author bik
 */
@Data
@TableName("cart")
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    /**
     * 关联商品 id
     */
    @TableField("goods_id")
    private Integer goodsId;

    /**
     * 关联 sku id（若无 SKU，可为 null / 0）
     */
    @TableField("sku_id")
    private Integer skuId;

    /**
     * 规格类型 0 单规格 1 多规格
     */
    @TableField("skus_type")
    private Boolean skusType;

    /**
     * 数量
     */
    @TableField("num")
    private Integer num;

    @TableField("create_time")
    private Long createTime;

    @TableField("update_time")
    private Long updateTime;
}
