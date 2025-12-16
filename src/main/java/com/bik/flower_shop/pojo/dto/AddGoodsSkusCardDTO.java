package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class AddGoodsSkusCardDTO {
    private Integer goodsId;
    private String name;
    private Integer order;
    private Byte type;
}
