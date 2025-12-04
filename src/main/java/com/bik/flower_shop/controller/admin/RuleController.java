package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class RuleController {

    // GET /admin/rule/:page
    @GetMapping("/rule/{page}")
    public ApiResult<?> listRules(@PathVariable int page) {
        return ApiResult.ok();
    }

    // POST /admin/rule/:id/delete
    @PostMapping("/rule/{id}/delete")
    public ApiResult<?> deleteRule(@PathVariable Long id) {
        return ApiResult.ok();
    }

    // POST /admin/rule
    @PostMapping("/rule")
    public ApiResult<?> createRule(@RequestBody Object body) {
        return ApiResult.ok();
    }

    // POST /admin/rule/:id
    @PostMapping("/rule/{id}")
    public ApiResult<?> updateRule(@PathVariable Long id, @RequestBody Object body) {
        return ApiResult.ok();
    }

    // POST /admin/rule/:id/update_status
    @PostMapping("/rule/{id}/update_status")
    public ApiResult<?> updateRuleStatus(@PathVariable Long id) {
        return ApiResult.ok();
    }
}
