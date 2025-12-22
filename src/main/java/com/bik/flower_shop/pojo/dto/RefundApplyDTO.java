package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class RefundApplyDTO {
    private Integer orderId;
    private String reason;
    private String refundType;
}