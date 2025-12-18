package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
public class SalesVO {
    private Integer saleCount;
    private Integer reviewCount;
    private BigDecimal rating;
}
