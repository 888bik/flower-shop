package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.pojo.dto.RuleDTO;
import com.bik.flower_shop.pojo.entity.Rule;
import com.bik.flower_shop.service.RuleService;
import com.bik.flower_shop.common.ApiResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/admin/rule")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    public ApiResult<Rule> createRule(@ModelAttribute RuleDTO dto) {
        Rule rule = ruleService.createRule(dto);
        return ApiResult.ok(rule);
    }

    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> getRuleList(@PathVariable Integer page) {
        List<Map<String, Object>> menuTree = ruleService.getMenuTree();
        Map<String, Object> result = new HashMap<>();
        result.put("list", menuTree);
        result.put("totalCount", menuTree.size());
        return ApiResult.ok(result);
    }

    @PostMapping("/{id}")
    public ApiResult<Boolean> updateRule(@PathVariable Integer id,
                                         @ModelAttribute RuleDTO dto) {

        boolean success = ruleService.updateRule(id, dto);
        return ApiResult.ok(success);
    }


    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> deleteRule(@RequestHeader("token") String token,
                                         @PathVariable Integer id) {
        boolean success = ruleService.deleteRule(id);
        return ApiResult.ok(success);
    }

    @PostMapping("/{id}/update_status")
    public ApiResult<Boolean> updateRuleStatus(@RequestHeader("token") String token,
                                               @PathVariable Integer id,
                                               @RequestParam("status") Byte status) {
        boolean success = ruleService.updateStatus(id, status);
        return ApiResult.ok(success);
    }

}
