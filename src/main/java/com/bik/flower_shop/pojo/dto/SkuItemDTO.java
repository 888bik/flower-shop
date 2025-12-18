package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author bik
 */
@Data
public class SkuItemDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer skuId;
    private String image;              // SKU 对应图片（可空）
    private BigDecimal price;          // SKU 价格
    private Integer stock;             // SKU 库存
    private List<SkuSpecDTO> specs;    // 如 [{key: "颜色", value: "粉色"}]
}