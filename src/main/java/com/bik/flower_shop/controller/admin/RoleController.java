package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.RoleDTO;
import com.bik.flower_shop.pojo.dto.SetRoleRulesDTO;
import com.bik.flower_shop.pojo.entity.Role;
import com.bik.flower_shop.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/role")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ApiResult<Role> createRole(@RequestHeader("token") String token, @RequestBody RoleDTO dto) {
        Role role = roleService.createRole(dto);
        return ApiResult.ok(role);
    }

    @PostMapping("{id}")
    public ApiResult<Boolean> updateRole(
            @RequestHeader("token") String token,
            @PathVariable Integer id,
            @RequestBody RoleDTO dto
    ) {
        boolean success = roleService.updateRole(id, dto);
        return ApiResult.ok(success);

    }


    @GetMapping("{page}")
    public ApiResult<?> getRoleList(@PathVariable Integer page,
                                    @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResult.ok(roleService.getRoleList(page, limit));
    }

    @PostMapping("/{id}/delete")
    public Object deleteRole(@PathVariable Integer id) {
        boolean success = roleService.deleteRole(id);
        return ApiResult.ok(success);
    }

    @PostMapping("/{id}/update_status")
    public ApiResult<Boolean> updateStatus(
            @RequestHeader("token") String token,
            @PathVariable Integer id,
            @RequestParam Byte status
    ) {
        boolean success = roleService.updateStatus(id, status);
        return ApiResult.ok(success);
    }

    @PostMapping("/set_rules")
    public ApiResult<Boolean> setRules(
            @RequestHeader("token") String token,
            @RequestBody SetRoleRulesDTO dto
    ) {
        boolean success = roleService.setRoleRules(dto.getId(), dto.getRuleIds());
        return ApiResult.ok(success);
    }


}
