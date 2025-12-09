package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * 新增规格卡片值 DTO
 * @author bik
 */
@Data
public class CreateGoodsSkusCardValueDTO {

    private Integer goodsSkusCardId;


    private String name;


    private String value;


    private Integer order;
}
