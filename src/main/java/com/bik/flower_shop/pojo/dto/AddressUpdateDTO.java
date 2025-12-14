package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


/**
 * @author bik
 */
@Data
public class AddressUpdateDTO {
    @NotBlank
    private String province;
    @NotBlank
    private String city;
    @NotBlank
    private String district;
    @NotBlank
    private String address;
    private Integer zip;
    @NotBlank
    private String name;
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    private String phone;
    private Byte isDefault = 0;
}