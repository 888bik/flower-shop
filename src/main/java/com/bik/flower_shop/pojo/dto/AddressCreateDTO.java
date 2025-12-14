package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author bik
 */
@Data
public class AddressCreateDTO {
    @NotBlank
    private String province;
    @NotBlank private String city;
    @NotBlank private String district;
    @NotBlank private String address;
    private Integer zip;
    @NotBlank private String name;
    @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$") private String phone;
    // 0 or 1
    private Byte isDefault = 0;
}