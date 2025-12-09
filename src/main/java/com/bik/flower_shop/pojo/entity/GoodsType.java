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
@TableName("goods_type")
public class GoodsType implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField(value = "`order`")
    private Integer order;

    /**
     * 状态：0禁用1启用
     */
    @TableField("status")
    private Boolean status;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;
}
