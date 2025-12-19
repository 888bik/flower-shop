package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class OrderListQueryDTO {
    private Integer page;
    private Integer limit;
    private String tab;
    private String no;
//    private String starttime;
//    private String endtime;
    private String name;
    private String phone;
}
