package com.bik.flower_shop.pojo.vo;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class AddressVO {
    private Integer id;
    private Integer userId;
    private String province;
    private String city;
    private String district;
    private String address;
    private Integer zip;
    private String name;
    private String phone;
    private Byte isDefault;
    private Integer lastUsedTime;
    private Integer createTime;
    private Integer updateTime;
}