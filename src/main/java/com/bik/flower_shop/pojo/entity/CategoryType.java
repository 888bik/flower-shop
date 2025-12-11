package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author bik
 */
@Data
@TableName("category_type")
public class CategoryType {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private String name;
    private Byte status;
    @TableField(value = "`order`")
    private Integer order;
    private Integer createTime;
    private Integer updateTime;
}
