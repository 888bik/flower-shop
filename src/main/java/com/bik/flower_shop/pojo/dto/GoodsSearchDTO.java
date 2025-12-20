package com.bik.flower_shop.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsSearchDTO {
    private Integer id;
    private String title;
    private String cover;
    private BigDecimal minPrice;
    private BigDecimal minOprice;
    private Double rating;
    private Integer saleCount;
    private Integer reviewCount;
    private String description;
    private String unit;
}
