package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 创建订单 DTO
 * @author bik
 */
@Data
public class OrderCreateDTO {
    @NotEmpty
    private List<OrderCreateItemDTO> items;

    @NotBlank
    // 简化为字符串地址（前端已拼好 province/city/district/address）
    private String address;

    private String remark;

    // 可选：优惠券 id / 支付方式等
    private Integer couponUserId;
    private String paymentMethod;
}
