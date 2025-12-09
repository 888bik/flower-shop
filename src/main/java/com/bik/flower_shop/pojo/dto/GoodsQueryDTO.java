package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class GoodsQueryDTO {
    private Integer page = 1;
    private Integer limit = 10;
    private String tab = "all";
    private String title;
    private Integer categoryId;
}
