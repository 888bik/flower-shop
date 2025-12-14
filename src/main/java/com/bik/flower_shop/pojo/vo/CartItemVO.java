package com.bik.flower_shop.pojo.vo;

import lombok.Data;

@Data
public class CartItemVO {

    private Integer id;
    private Integer userId;
    private Integer goodsId;
    private Integer skuId;
    private Boolean skusType;
    private Integer num;
    private Long createTime;
    private Long updateTime;


    private String title;
    private String cover;
    private String unit;
    private String description;
    private String minPrice;
    private String minOprice;
    private Integer stock;
    private Integer saleCount;
    private Double rating;
    // 是否有效（商品审核）
    private Boolean valid = true;

    // sku 名称，如颜色/规格
    private String skuTitle;
    private Integer skuStock;


    // 前端默认勾选
    private Boolean checked = true;
}
