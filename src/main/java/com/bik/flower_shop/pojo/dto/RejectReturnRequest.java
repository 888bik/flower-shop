package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class RejectReturnRequest {
    private Integer orderId;
    private String reason;
}