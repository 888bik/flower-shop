package com.bik.flower_shop.pojo.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 新增/修改优惠券请求参数
 */
@Data
public class CouponDTO {
    private String name;
    private Byte type;
    private BigDecimal value;
    private Integer total;
    private BigDecimal minPrice;
    private Integer startTime;
    private Integer endTime;
    private Integer order;
    private String description;
}
