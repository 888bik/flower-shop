package com.bik.flower_shop.pojo.vo;

import com.bik.flower_shop.pojo.entity.Category;
import com.bik.flower_shop.pojo.entity.GoodsAttrs;
import com.bik.flower_shop.pojo.entity.GoodsBanner;
import com.bik.flower_shop.pojo.entity.GoodsSkus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bik
 */
@Data
public class GoodsVO {
    private Integer id;
    private String title;
    private Integer categoryId;
    private List<Integer> categoryIds;
    // 多分类
    private List<Category> categories;
    private String cover;


    private BigDecimal rating;

    private Integer saleCount;
    private Integer reviewCount;


    private String minPrice;
    private String minOprice;


    private String description;

    private String unit;
    private Integer stock;
    private Integer minStock;


    private Byte ischeck;
    private Byte status;
    private Byte stockDisplay;

    private Integer expressId;
    private Byte skuType;


    private Object skuValue;

    private String content;


    private BigDecimal discount;


    private Long createTime;
    private Long updateTime;
    private Long deleteTime;

    private Integer order;

    // 关联 VO
    private Category category;
    private List<GoodsBanner> goodsBanner = new ArrayList<>();
    private List<GoodsAttrs> goodsAttrs = new ArrayList<>();
    private List<GoodsSkusVO> goodsSkus = new ArrayList<>();
    private List<GoodsSkusCardVO> goodsSkusCard = new ArrayList<>();
}
