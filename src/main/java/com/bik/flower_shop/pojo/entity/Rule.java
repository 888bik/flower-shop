package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
     * 上级菜单ID
     */
    @TableField("rule_id")
    private Integer ruleId;

    /**
     * 状态0关闭1启用
     */
    @TableField("status")
    private Byte status;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;

    /**
     * 菜单/权限名称
     */
    @TableField("name")
    private String name;

    /**
     * 前端路由name值
     */
    @TableField(value = "`desc`")
    private String description;

    /**
     * 前台路由注册路径,menu=1且ruLeId>o时必填
     */
    @TableField("frontpath")
    private String frontpath;

    /**
     *  后端规则接口别名
     */
    @TableField(value = "`condition`")
    private String condition;

    /**
     * 是否显示为菜单：0不显示1关闭
     */
    @TableField("menu")
    private Byte menu;

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
