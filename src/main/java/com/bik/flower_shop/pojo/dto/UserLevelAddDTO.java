package com.bik.flower_shop.pojo.dto;

import lombok.Data;

@Data
public class UserLevelAddDTO {
    private String name;       // 等级名称
    private Integer level;     // 等级权重
    private Boolean status;    // 状态：true启用，false禁用
    private Integer discount;  // 折扣（百分比，比如100表示不打折，95表示95%）
    private Integer maxPrice;  // 消费满金额
    private Integer maxTimes;  // 消费满次数
}
