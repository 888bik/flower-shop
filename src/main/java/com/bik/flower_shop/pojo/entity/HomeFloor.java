package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@TableName("home_floor")
@Data
public class HomeFloor {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private Integer categoryId;

    private String bannerImage;

    private Integer productLimit;

    private Integer sort;

    private Byte status;

    private Long createTime;

    private Long updateTime;

    /** 楼层商品（非数据库字段） */
    @TableField(exist = false)
    private List<Goods> products;
}
