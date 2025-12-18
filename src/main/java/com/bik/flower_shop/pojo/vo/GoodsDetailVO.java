package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class GoodsDetailVO {

    private Integer id;
    private String title;
    private String cover;
    private List<String> images;

    /** 分类 */
    private List<CategoryVO> categories;

    /** 价格信息 */
    private PriceVO price;

    /** SKU 信息 */
    private SkuVO sku;

    /** 库存 */
    private StockVO stock;

    /** 销量 / 评价 */
    private SalesVO sales;

    private String unit;
    private String content;

    private Long createTime;
}
