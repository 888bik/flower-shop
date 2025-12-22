package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class UserLevelDTO {
    private Integer id;
    private String name;
    private Integer level;
    private Integer status;
    private Integer discount;
    private Integer maxPrice;
    private Integer maxTimes;
}
