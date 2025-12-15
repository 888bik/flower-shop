package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单项 DTO（客户端提供 goodsId/skuId/num）
 * @author bik
 */
@Data
public class OrderCreateItemDTO {
    @NotNull
    private Integer goodsId;

    private Integer skuId;

    @NotNull
    @Min(1)
    private Integer num;
}
