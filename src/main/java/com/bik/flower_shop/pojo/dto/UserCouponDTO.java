package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
public class UserCouponDTO {
    private Integer id;
    private String name;
    private Byte type;
    private BigDecimal value;
    // 前端"limit"
    private BigDecimal minPrice;
    private Integer total;
    private Integer used;
    private Integer startTime;
    private Integer endTime;
    private Integer order;
    // scope / desc
    private String description;
    // ── 计算字段，用于前端
    // 当前用户是否已领取
    private Boolean received;
    // total - used
    private Integer stock;

    private String scope;
    private String time;
}
