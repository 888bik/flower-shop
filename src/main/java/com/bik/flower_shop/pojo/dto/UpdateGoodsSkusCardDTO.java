package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * 修改规格卡片 DTO
 */
@Data
public class UpdateGoodsSkusCardDTO {
    private Integer goodsId;
    private String name;
    private Integer order;
    /**
     * 规格类型：0 文字、1 颜色、2 图片 等
     */
    private Byte type;
}
