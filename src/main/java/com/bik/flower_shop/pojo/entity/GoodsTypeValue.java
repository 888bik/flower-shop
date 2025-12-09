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
 *
 * @author bik
 * @since 2025-12-04
 */
@Data
@TableName("goods_type_value")
public class GoodsTypeValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品类型属性名称
     */
    @TableField("name")
    private String name;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;

    /**
     * 表单类型
     */
    @TableField("type")
    private String type;

    /**
     * 状态：0禁止1开启
     */
    @TableField("status")
    private Boolean status;

    /**
     * 默认值/选项值
     */
    @TableField("default")
    private String Default;

    /**
     * 商品类型id
     */
    @TableField("goods_type_id")
    private Integer goodsTypeId;
}
