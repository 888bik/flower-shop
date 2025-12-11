package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author bik
 */
@Getter
@Setter
@TableName("goods_category")
public class GoodsCategory implements Serializable {
    @TableField("goods_id")
    private Integer goodsId;
    @TableField("category_id")
    private Integer categoryId;
}
