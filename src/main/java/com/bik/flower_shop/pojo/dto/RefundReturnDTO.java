package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class RefundReturnDTO {
    private Integer orderId;
    private String reason;
}
