package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class GoodsQueryDTO {
    private Integer page = 1;
    private Integer limit = 10;
    private String tab = "all";
    private String title;
    // 保留单一 categoryId 用于兼容旧数据
    private Integer categoryId;
    private List<Integer> categoryIds;
}
