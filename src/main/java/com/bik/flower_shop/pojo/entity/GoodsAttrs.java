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
@TableName("goods_attrs")
public class GoodsAttrs implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品id
     */
    @TableField("goods_id")
    private Integer goodsId;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;

    /**
     * 属性值
     */
    @TableField("value")
    private String value;

    /**
     * 商品类型属性名称
     */
    @TableField("name")
    private String name;

    @TableField(value = "`default`")
    private String Default;

    /**
     * 表单类型
     */
    @TableField("type")
    private String type;
}
