package com.bik.flower_shop.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer saleCount;
    private Integer reviewCount;
    private BigDecimal rating;
}
