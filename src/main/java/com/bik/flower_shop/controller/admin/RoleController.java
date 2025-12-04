package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class RoleController {

    @PostMapping("/role/{id}/delete")
    public ApiResult<?> deleteRole(@PathVariable Long id) {
        return ApiResult.ok();
    }

    @GetMapping("/role/{page}")
    public ApiResult<?> listRole(@PathVariable int page) {
        return ApiResult.ok();
    }

    @PostMapping("/role")
    public ApiResult<?> createRole(@RequestBody Object body) {
        return ApiResult.ok();
    }

    @PostMapping("/role/set_rules")
    public ApiResult<?> setRoleRules(@RequestParam Long roleId, @RequestBody Object rules) {
        return ApiResult.ok();
    }

    @PostMapping("/role/{id}")
    public ApiResult<?> updateRole(@PathVariable Long id, @RequestBody Object body) {
        return ApiResult.ok();
    }

    @PostMapping("/role/{id}/update_status")
    public ApiResult<?> updateRoleStatus(@PathVariable Long id) {
        return ApiResult.ok();
    }
}
