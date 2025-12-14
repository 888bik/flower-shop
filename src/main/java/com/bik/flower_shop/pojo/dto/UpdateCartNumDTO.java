package com.bik.flower_shop.pojo.dto;

import lombok.Data;

@Data
public class UpdateCartNumDTO {
    // 购物车项ID
    private Integer cartId;
    // 修改后的数量
    private Integer num;
}