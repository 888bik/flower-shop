package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String repassword;
    private String email;
}
