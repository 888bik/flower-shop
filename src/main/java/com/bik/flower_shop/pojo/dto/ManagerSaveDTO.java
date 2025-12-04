package com.bik.flower_shop.pojo.dto;
import lombok.Data;

@Data
public class ManagerSaveDTO {
    private String username;
    private String password;
    private String avatar;
    private Integer roleId;
    private Byte status;
}
