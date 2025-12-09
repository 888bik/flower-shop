package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class ManagerDTO {
    private Integer id;
    private Byte status;
    private String username;
    private String avatar;

    private Boolean superAdmin;
    private String createTime;
    private String updateTime;

    private Integer roleId;
    private String roleName;

    // 返回给前端的 role 对象
    private RoleSimpleDTO role;

    // 构建 role 对象
    public void buildRole() {
        if (roleId != null && roleName != null) {
            RoleSimpleDTO r = new RoleSimpleDTO();
            r.setId(roleId);
            r.setName(roleName);
            this.role = r;
        }
    }
}
