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
@TableName("category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("status")
    private Byte status;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;

    /**
     * 父级分类id
     */
    @TableField("category_id")
    private Integer categoryId;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;

    // 新增：分类类型，例如 "用途"、"品种"、"场景"
    @TableField("type")
    private String type;

    @TableField("type_id")
    private Integer typeId;

    @TableField(exist = false)
    private String typeName;

}
