package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 修改商品 DTO（精简字段）
 * @author bik
 */
@Data
public class UpdateGoodsDTO {
    private String title;
    private Integer categoryId;
    private String cover;
    private String description;
    private String unit;
    private Integer stock;
    private Integer minStock;
    private Byte status;
    private Byte stockDisplay;
    private BigDecimal minPrice;
    private BigDecimal minOprice;
}
