package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class OrderAddressDTO {
    /**
     * 用户地址Id
     */
    private Integer id;
    private Integer userId;
    private String province;
    private String city;
    private String district;
    private String address;
    private String zip;
    private String name;
    private String phone;
}
