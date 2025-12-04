package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

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
@TableName("goods")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品名称
     */
    @TableField("title")
    private String title;

    /**
     * 分类id
     */
    @TableField("category_id")
    private Integer categoryId;

    /**
     * 商品封面图
     */
    @TableField("cover")
    private String cover;

    /**
     * 平均评分
     */
    @TableField("rating")
    private Double rating;

    /**
     * 总销量
     */
    @TableField("sale_count")
    private Integer saleCount;

    /**
     * 评论数
     */
    @TableField("review_count")
    private Integer reviewCount;

    /**
     * 最低sku价格
     */
    @TableField("min_price")
    private BigDecimal minPrice;

    @TableField("min_oprice")
    private BigDecimal minOprice;

    /**
     * 商品描述
     */
    @TableField(value = "`desc`")
    private String description;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 库存
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 库存预警
     */
    @TableField("min_stock")
    private Integer minStock;

    /**
     * 是否审核 0审核中 1通过 2拒绝
     */
    @TableField("ischeck")
    private Boolean ischeck;

    /**
     * 状态 0仓库1上架
     */
    @TableField("status")
    private Boolean status;

    /**
     * 库存显示 0隐藏 1显示
     */
    @TableField("stock_display")
    private Boolean stockDisplay;

    /**
     * 运费模板id
     */
    @TableField("express_id")
    private Integer expressId;

    /**
     * sku类型：0单一，1多规格
     */
    @TableField("sku_type")
    private Boolean skuType;

    /**
     * 单一规格值
     */
    @TableField("sku_value")
    private String skuValue;

    /**
     * 商品详情
     */
    @TableField("content")
    private String content;

    /**
     * 折扣设置
     */
    @TableField("discount")
    private Double discount;

    @TableField("create_time")
    private Integer createTime;

    @TableField("update_time")
    private Integer updateTime;

    @TableField("delete_time")
    private Integer deleteTime;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;
}
