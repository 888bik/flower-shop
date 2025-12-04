package com.bik.flower_shop.pojo.vo;
import lombok.Data;

@Data
public class ManagerInfoVO {
    private Integer id;
    private String username;
    private String avatar;
    private Integer superAdmin;
    private Object role;
    private Object menus;
    private Object ruleNames;
}
