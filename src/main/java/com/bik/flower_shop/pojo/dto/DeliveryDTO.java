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
public class DeliveryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer expressId;
    // 预估快递费（若你用模板计算，可返回 null 或 0）
    private BigDecimal fee;
    // 模板名称（可选）
    private String templateName;
}
