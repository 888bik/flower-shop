package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author bik
 */
@Data
public class UpdateSkusDTO {

    // 0 单规格，1 多规格
    private Integer skuType;

    // 单规格信息
    private SkuValueDTO skuValue;

    // 多规格列表
    private List<GoodsSkuDTO> goodsSkus;

    // 可选：规格卡片（多规格时，用来更新卡片 + 值）
    private List<GoodsSkusCardDTO> goodsSkusCard;


    @Data
    public static class SkuValueDTO {
        private BigDecimal oprice;
        private BigDecimal pprice;
        private BigDecimal cprice;
        private BigDecimal weight;
        private BigDecimal volume;
    }

    @Data
    public static class GoodsSkuDTO {
        // 前端可能传 object/map 或 数组，使用 Object 或 Map 来接收更灵活
        // 最终我们会把它序列化为 JSON 字符串存到 goods_skus.skus 列
        private Object skus;
        private String image;
        private BigDecimal pprice;
        private BigDecimal oprice;
        private BigDecimal cprice;
        private Integer stock;
        private BigDecimal volume;
        private BigDecimal weight;
        private String code;
        private Integer goodsId;
        private Integer id; // 可选，前端如果传 id 表示编辑现有 skl 行（示例中按删除重建）
    }

    @Data
    public static class GoodsSkusCardDTO {
        private Integer id; // 可选
        private String name;
        private Byte type; // 0/1/2 等，按你表结构
        private Integer order;
        private List<GoodsSkusCardValueDTO> goodsSkusCardValue;
    }

    @Data
    public static class GoodsSkusCardValueDTO {
        private Integer id; // 可选
        private String name;
        private String value;
        private Integer order;
    }
}
