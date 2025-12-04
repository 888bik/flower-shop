package com.bik.flower_shop.pojo.dto;
import lombok.Data;

@Data
public class UpdatePasswordDTO {
    private String oldPassword;
    private String password;
    private String rePassword;
}
