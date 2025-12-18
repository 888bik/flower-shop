package com.bik.flower_shop.pojo.dto;

import com.bik.flower_shop.pojo.DeliveryDTO;
import com.bik.flower_shop.pojo.dto.PriceDTO;
import com.bik.flower_shop.pojo.dto.SimpleCategoryDTO;
import com.bik.flower_shop.pojo.dto.SkuDTO;
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
    private String subtitle;           // 可选副标题
    private SimpleCategoryDTO category;
    private String cover;              // 封面图
    private List<String> banners;       // 轮播图


    private PriceDTO price;            // 价格信息
    private SkuDTO sku;                // SKU 信息（items + cards）
    private StockDTO stock;            // 库存信息
    private SalesDTO sales;            // 销量/评分信息

    private String unit;               // 单位
    private String contentHtml;        // 商品详情 HTML（已经过白名单过滤）
    private DeliveryDTO delivery;      // 运费/配送信息

    private Boolean isAvailable;       // 是否可购买（上架 + 审核 + 库存判断等）
    private Long createTime;           // 时间戳（秒或毫秒，按你项目约定）

    private Integer likeCount;
    private Boolean isFavorite;   // 当前登录用户是否收藏
}
