package com.bik.flower_shop.pojo.dto;

import lombok.Data;

@Data
public class GoodsQueryDTO {
    private Integer page = 1;
    private Integer limit = 10;
    private String tab = "all";    // all / checking / saling / off / min_stock / delete
    private String title;
    private Integer categoryId;
}
