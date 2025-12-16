package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
public class OrderExtraDTO {
    private ShippingInfo shipping;

    @Data
    public static class ShippingInfo {
        private String type;   // standard / express
        private String name;
        private BigDecimal fee;
    }
}
