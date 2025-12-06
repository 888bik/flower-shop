package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.mapper.RuleMapper;
import com.bik.flower_shop.pojo.dto.RuleDTO;
import com.bik.flower_shop.pojo.entity.Rule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * @author bik
 */
@Service
public class RuleService {

    private final RuleMapper ruleMapper;

    public RuleService(RuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    public Rule createRule(RuleDTO dto) {
        System.out.println("11111111111111111111");
        System.out.println(dto);
        Rule rule = new Rule();
        rule.setRuleId(dto.getRuleId());
        rule.setMenu(dto.getMenu());
        rule.setName(dto.getName());
        rule.setCondition(dto.getCondition());
        rule.setMethod(dto.getMethod());
        rule.setStatus(dto.getStatus());
        rule.setOrder(dto.getOrder() != null ? dto.getOrder() : 50);
        rule.setIcon(dto.getIcon());
        rule.setFrontpath(dto.getFrontpath());

        int now = (int) Instant.now().getEpochSecond();
        rule.setCreateTime(now);
        rule.setUpdateTime(now);

        ruleMapper.insert(rule);
        return rule;
    }

    public List<Map<String, Object>> getMenuTree() {
        List<Rule> rules = ruleMapper.selectAllRules();
        return buildTree(rules, 0);
    }

    private List<Map<String, Object>> buildTree(List<Rule> rules, Integer parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Rule r : rules) {
            if (Objects.equals(r.getRuleId(), parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", r.getId());
                node.put("rule_id", r.getRuleId());
                node.put("name", r.getName());
                node.put("desc", r.getDescription());
                node.put("menu", r.getMenu());
                node.put("status", r.getStatus());
                node.put("frontpath", r.getFrontpath());
                node.put("condition", r.getCondition());
                node.put("method", r.getMethod());
                node.put("order", r.getOrder());
                node.put("icon", r.getIcon());
                node.put("create_time", r.getCreateTime());
                node.put("update_time", r.getUpdateTime());
                // 递归获取子节点
                List<Map<String, Object>> children = buildTree(rules, r.getId());
                if (!children.isEmpty()) {
                    node.put("child", children);
                }
                tree.add(node);
            }
        }
        // 按 order 排序
        tree.sort(Comparator.comparingInt(n -> (Integer) n.getOrDefault("order", 0)));
        return tree;
    }

    public List<Map<String, Object>> getRuleTree() {
        List<Rule> rules = ruleMapper.selectAllRules();
        return buildRuleTree(rules, 0);
    }

    private List<Map<String, Object>> buildRuleTree(List<Rule> rules, Integer parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Rule r : rules) {
            if (Objects.equals(r.getRuleId(), parentId) && r.getMenu() != null && r.getMenu() != 0) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", r.getId());
                node.put("rule_id", r.getRuleId());
                node.put("name", r.getName());
                node.put("desc", r.getDescription());
                node.put("menu", r.getMenu());
                node.put("status", r.getStatus());
                node.put("frontpath", r.getFrontpath());
                node.put("condition", r.getCondition());
                node.put("method", r.getMethod());
                node.put("order", r.getOrder());
                node.put("icon", r.getIcon());
                node.put("create_time", r.getCreateTime());
                node.put("update_time", r.getUpdateTime());

                // 递归获取子节点
                List<Map<String, Object>> children = buildRuleTree(rules, r.getId());
                if (!children.isEmpty()) {
                    node.put("child", children);
                }
                tree.add(node);
            }
        }
        // 按 order 排序
        tree.sort(Comparator.comparingInt(n -> (Integer) n.getOrDefault("order", 0)));
        return tree;
    }


    @Transactional
    public boolean updateRule(Integer id, RuleDTO dto) {

        Rule rule = ruleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }

        // 设置参数
        rule.setRuleId(dto.getRuleId() != null ? dto.getRuleId() : 0);
        rule.setMenu(dto.getMenu() != null ? dto.getMenu() : 0);
        rule.setName(dto.getName());
        rule.setCondition(dto.getCondition());
        rule.setMethod(dto.getMethod());
        rule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        rule.setOrder(dto.getOrder() != null ? dto.getOrder() : 50);
        rule.setIcon(dto.getIcon());
        rule.setFrontpath(dto.getFrontpath());
        rule.setUpdateTime((int) Instant.now().getEpochSecond());

        return ruleMapper.updateById(rule) > 0;
    }


    @Transactional
    public boolean deleteRule(Integer id) {
        // 先查询是否有子菜单
        QueryWrapper<Rule> query = new QueryWrapper<>();
        query.eq("rule_id", id);
        List<Rule> children = ruleMapper.selectList(query);
        if (children != null && !children.isEmpty()) {
            // 存在子菜单，不能删除
            throw new RuntimeException("该菜单下存在子菜单，不能删除！");
        }

        // 没有子菜单才删除
        return ruleMapper.deleteById(id) > 0;
    }

    @Transactional
    public boolean updateStatus(Integer id, Byte status) {
        Rule rule = new Rule();
        rule.setId(id);
        rule.setStatus(status);
        int updated = ruleMapper.updateById(rule);
        return updated > 0;
    }

}
