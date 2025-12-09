package com.bik.flower_shop.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 前端传入的多规格DTO
 */
@Data
public class GoodsSkusDTO {

    /**
     * sku组合信息 [{goods_skus_card_id,name,value,id,text}, ...]
     */
    private List<Map<String, Object>> skus;

    /**
     * 图片
     */
    private String image;

    /**
     * 市场价
     */
    private BigDecimal oprice;

    /**
     * 售价
     */
    private BigDecimal pprice;

    /**
     * 成本价
     */
    private BigDecimal cprice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 体积
     */
    private BigDecimal volume;

    /**
     * 重量
     */
    private BigDecimal weight;

    /**
     * 商品编码
     */
    private String code;


}
