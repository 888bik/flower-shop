package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class OrderPageQueryDTO {
    private Integer page = 1;
    private Integer limit = 12;
}
