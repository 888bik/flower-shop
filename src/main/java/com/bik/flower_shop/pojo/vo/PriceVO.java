package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
public class PriceVO {
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal originalMin;
    private BigDecimal discount;
}
