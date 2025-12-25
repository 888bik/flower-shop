package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单 DTO
 *
 * @author bik
 */
@Data
public class OrderCreateDTO {
    @NotEmpty
    private List<OrderCreateItemDTO> items;

    @NotNull
    private Integer addressId;

    private String remark;

    @NotEmpty
    private String shippingType;

    private Integer couponUserId;
}
