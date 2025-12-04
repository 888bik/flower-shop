package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author bik
 * @since 2025-12-04
 */
@Data
@TableName("rule")
public class Rule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 父级id
     */
    @TableField("rule_id")
    private Integer ruleId;

    /**
     * 状态0关闭1启用
     */
    @TableField("status")
    private Boolean status;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;

    @TableField("name")
    private String name;

    /**
     * 前端路由name值
     */
    @TableField(value = "`desc`")
    private String description;

    /**
     * 前台路由注册路径
     */
    @TableField("frontpath")
    private String frontpath;

    @TableField(value = "`condition`")
    private String condition;

    /**
     * 是否显示为菜单：0不显示1关闭
     */
    @TableField("menu")
    private Boolean menu;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 请求类型
     */
    @TableField("method")
    private String method;

    @TableField(exist = false)
    private List<Rule> child;

}
