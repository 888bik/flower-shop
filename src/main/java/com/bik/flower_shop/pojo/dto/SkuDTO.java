package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class SkuDTO {
    // 0: single, 1: multi
    private Byte type;
    // 扁平化的 SKU 列表（用于加入购物车/立即购买）
    private List<SkuItemDTO> items;
    // 规格卡（用于构建规格选择器）
    private List<SkuCardDTO> cards;
}