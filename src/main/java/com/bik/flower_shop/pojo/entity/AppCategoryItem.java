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
@TableName("app_category_item")
public class AppCategoryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("cover")
    private String cover;

    @TableField("category_id")
    private Integer categoryId;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField(value = "`order`")
    private Integer order;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;
}
