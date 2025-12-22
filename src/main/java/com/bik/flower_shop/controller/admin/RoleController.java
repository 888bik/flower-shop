package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.RoleDTO;
import com.bik.flower_shop.pojo.dto.SetRoleRulesDTO;
import com.bik.flower_shop.pojo.dto.StatusDTO;
import com.bik.flower_shop.pojo.entity.Role;
import com.bik.flower_shop.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@AuthRequired(role = "admin")
@RequestMapping("/admin/role")
public class RoleController {

    private final RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    public ApiResult<Role> createRole(@RequestHeader("token") String token, @RequestBody RoleDTO dto) {
        Role role = roleService.createRole(dto);
        return ApiResult.ok(role);
    }

    /**
     * 修改角色
     */
    @PostMapping("{id}")
    public ApiResult<Boolean> updateRole(
            @RequestHeader("token") String token,
            @PathVariable Integer id,
            @RequestBody RoleDTO dto
    ) {
        boolean success = roleService.updateRole(id, dto);
        return ApiResult.ok(success);

    }

    /**
     * 获取角色列表
     */
    @GetMapping("{page}")
    public ApiResult<?> getRoleList(@PathVariable Integer page,
                                    @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResult.ok(roleService.getRoleList(page, limit));
    }

    /**
     * 删除角色
     */
    @PostMapping("/{id}/delete")
    public Object deleteRole(@PathVariable Integer id) {
        boolean success = roleService.deleteRole(id);
        return ApiResult.ok(success);
    }

    /**
     * 修改角色状态
     */
    @PostMapping("/{id}/update_status")
    public ApiResult<Boolean> updateRoleStatus(
            @RequestHeader("token") String token,
            @PathVariable Integer id,
            @RequestBody StatusDTO dto
    ) {
        boolean success = roleService.updateStatus(id, dto.getStatus());
        return ApiResult.ok(success);
    }

    /**
     * 设置角色权限
     */
    @PostMapping("/set_rules")
    public ApiResult<Boolean> setRules(
            @RequestHeader("token") String token,
            @RequestBody SetRoleRulesDTO dto
    ) {
        boolean success = roleService.setRoleRules(dto.getId(), dto.getRuleIds());
        return ApiResult.ok(success);
    }


}
