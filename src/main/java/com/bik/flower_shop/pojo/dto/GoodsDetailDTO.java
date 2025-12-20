package com.bik.flower_shop.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author bik
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String title;
    private String description;
    private SimpleCategoryDTO category;
    // 封面图
    private String cover;
    // 轮播图
    private List<String> banners;

    // 价格信息
    private PriceDTO price;
    // SKU 信息（items + cards）
    private SkuDTO sku;
    // 库存信息
    private StockDTO stock;
    // 销量/评分信息
    private SalesDTO sales;
    // 单位
    private String unit;
    // 商品详情 HTML（已经过白名单过滤）
    private String contentHtml;
    // 运费/配送信息
//    private DeliveryDTO delivery;

    private Boolean isAvailable;
    private Long createTime;

    private Integer likeCount;
    // 当前登录用户是否收藏
    private Boolean isFavorite;
}
