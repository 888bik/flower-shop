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
@TableName("express_company")
public class ExpressCompany implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    /**
     * 物流公司代码 (快递100)
     */
    @TableField("code")
    private String code;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;
}
