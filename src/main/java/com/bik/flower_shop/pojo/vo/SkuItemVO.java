package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author bik
 */
@Data
public class SkuItemVO {
    private Integer skuId;
    private List<SkuSpecVO> specs;
    private BigDecimal price;
    private Integer stock;
    private String image;
}
