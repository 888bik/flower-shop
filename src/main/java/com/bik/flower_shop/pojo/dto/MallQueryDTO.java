package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class MallQueryDTO {
    private Integer page;
    private Integer limit;
    private Integer categoryId;
    private String title;
}
