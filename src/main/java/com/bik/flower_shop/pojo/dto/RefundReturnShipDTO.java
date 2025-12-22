package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 用户提交退货物流信息
 * @author bik
 */
@Data
public class RefundReturnShipDTO {

    @NotNull(message = "订单ID不能为空")
    private Integer orderId;

    @NotBlank(message = "物流公司不能为空")
    private String company;

    @NotBlank(message = "物流单号不能为空")
    private String trackingNo;
}
