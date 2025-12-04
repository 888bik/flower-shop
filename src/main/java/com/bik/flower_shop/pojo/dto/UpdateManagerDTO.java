package com.bik.flower_shop.pojo.dto;

import lombok.Data;

@Data
public class UpdateManagerDTO {
    private String username;
    private String password;
    private Integer roleId;
    private Integer status;
    private String avatar;
}
