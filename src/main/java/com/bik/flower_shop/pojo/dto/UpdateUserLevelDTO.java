package com.bik.flower_shop.pojo.dto;

import lombok.Data;

@Data
public class UpdateUserLevelDTO {
    private String name;       // 等级名称
    private Integer level;     // 等级权重
    private Boolean status;    // 状态
    private Integer discount;  // 折扣
    private Integer maxPrice;  // 消费满金额
    private Integer maxTimes;  // 消费满次数
}
