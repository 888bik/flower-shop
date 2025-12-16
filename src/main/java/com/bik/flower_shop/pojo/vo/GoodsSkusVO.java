package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@Data
public class GoodsSkusVO {
    private Integer id;
    private String image;
    private BigDecimal pprice;
    private BigDecimal oprice;
    private BigDecimal cprice;
    private Integer stock;
    private BigDecimal volume;
    private BigDecimal weight;
    private String code;
    private Integer goodsId;

    private List<Map<String, Object>> skus;
}
