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
@TableName("goods_skus_card")
public class GoodsSkusCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("goods_id")
    private Integer goodsId;

    /**
     * 商品属性卡片值
     */
    @TableField("name")
    private String name;

    /**
     * 商品属性卡片属性类型 0无 1颜色 2图片
     */
    @TableField("type")
    private Byte type;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;
}
