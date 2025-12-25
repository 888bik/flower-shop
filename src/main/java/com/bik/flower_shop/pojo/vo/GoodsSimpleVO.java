package com.bik.flower_shop.pojo.vo;

import com.bik.flower_shop.pojo.entity.GoodsBanner;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 首页商品卡片 VO
 *
 * @author bik
 */
@Data
public class GoodsSimpleVO {

    private Integer id;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 封面图
     */
    private String cover;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 销量
     */
    private Integer saleCount;

    /**
     * 最低售价
     */
    private BigDecimal minPrice;

    /**
     * 原价
     */
    private BigDecimal minOprice;

    /**
     * 轮播图
     */
    private List<String> banners;
}
