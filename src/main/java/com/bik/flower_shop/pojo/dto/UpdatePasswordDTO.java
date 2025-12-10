package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class UpdatePasswordDTO {
    private String oldPassword;
    private String password;
    private String rePassword;
}
