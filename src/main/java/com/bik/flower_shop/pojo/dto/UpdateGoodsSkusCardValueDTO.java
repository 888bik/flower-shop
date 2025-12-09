package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class UpdateGoodsSkusCardValueDTO {
    private Integer goodsSkusCardId;
    private String name;
    private String value;
    private Integer order;
}