package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;


/**
 * @author bik
 */
@Data
public class CouponAdminDTO {
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
