package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新用户资料 DTO
 */
@Data
public class UpdateUserDTO {

    private String avatar;

    @Size(max = 20, message = "昵称最多 20 个字符")
    private String nickname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}
