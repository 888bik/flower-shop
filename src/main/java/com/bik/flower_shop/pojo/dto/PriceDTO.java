package com.bik.flower_shop.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal min;           // 最低价（数值，方便计算）
    private BigDecimal max;           // 最高价（如多规格）
    private BigDecimal originalMin;   // 原价或市场价
    private BigDecimal discount;      // 折扣率（如 0.9 表示 9 折）
    private String currency = "CNY";  // 货币
    private String displayMin;        // 格式化后的展示字符串，如 "¥271.00"
}
