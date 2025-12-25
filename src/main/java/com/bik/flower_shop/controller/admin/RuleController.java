package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.pojo.dto.RuleDTO;
import com.bik.flower_shop.pojo.dto.StatusDTO;
import com.bik.flower_shop.pojo.entity.Rule;
import com.bik.flower_shop.service.RuleService;
import com.bik.flower_shop.common.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@AuthRequired(role = "admin")
@RequestMapping("/admin/rule")
public class RuleController {

    private final RuleService ruleService;

    /**
     * 创建菜单权限
     */
    @PostMapping
    public ApiResult<Rule> createRule(@RequestBody RuleDTO dto) {
        Rule rule = ruleService.createRule(dto);
        return ApiResult.ok(rule);
    }

    /**
     * 获取菜单权限树
     */
    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> getRuleList(@PathVariable Integer page) {
        List<Map<String, Object>> menuTree = ruleService.getMenuTree();
        List<Map<String, Object>> rulesTree = ruleService.getRuleTree();

        Map<String, Object> result = new HashMap<>();
        result.put("list", menuTree);
        result.put("rules", rulesTree);
        result.put("totalCount", menuTree.size());
        return ApiResult.ok(result);
    }

    /**
     * 更新菜单权限
     */
    @PostMapping("/{id}")
    public ApiResult<Boolean> updateRule(@PathVariable Integer id,
                                         @RequestBody RuleDTO dto) {

        boolean success = ruleService.updateRule(id, dto);
        return ApiResult.ok(success);
    }

    /**
     * 删除菜单权限
     */
    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> deleteRule(@PathVariable Integer id) {
        boolean success = ruleService.deleteRule(id);
        return ApiResult.ok(success);
    }

    /**
     * 更新菜单权限状态
     */
    @PostMapping("/{id}/update_status")
    public ApiResult<Boolean> updateRuleStatus(@PathVariable Integer id,
                                               @RequestBody StatusDTO dto) {
        boolean success = ruleService.updateStatus(id, dto.getStatus());
        return ApiResult.ok(success);
    }
}
