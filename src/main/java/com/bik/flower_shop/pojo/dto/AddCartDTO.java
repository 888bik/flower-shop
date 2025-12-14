package com.bik.flower_shop.pojo.dto;// package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class AddCartDTO {
    // 必填
    private Integer goodsId;
    // 可选（多规格时必须）
    private Integer skuId;
    // 数量，默认 1
    private Integer num = 1;
}
