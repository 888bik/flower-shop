package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 发货请求
 */
@Data
public class ShipOrderRequest {
    /**
     * 优先使用公司 id，如果没有可传 name
     */
    private Integer expressCompanyId;

    /**
     * 可选：快递公司名称（当没有 expressCompanyId 时使用）
     */
    private String expressCompanyName;

    @NotBlank(message = "快递单号不能为空")
    private String expressNo;
}
