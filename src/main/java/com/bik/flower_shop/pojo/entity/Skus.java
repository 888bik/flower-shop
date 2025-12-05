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
@TableName("skus")
public class Skus implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 规格分类名称
     */
    @TableField("name")
    private String name;

    /**
     * 规格类型 0无 1颜色 2图片
     */
    @TableField("type")
    private Byte type;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;

    /**
     * 状态：0禁用1启用
     */
    @TableField("status")
    private Boolean status;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;

    @TableField("defaults")
    private String defaults;
}
