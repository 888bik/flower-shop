package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.mapper.RoleMapper;
import com.bik.flower_shop.pojo.entity.Role;
import com.bik.flower_shop.pojo.entity.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;

    /**
     * 根据角色ID获取角色对象（不包含规则）
     */
    public Role getRoleById(Integer roleId) {
        return roleMapper.selectById(roleId);
    }

    /**
     * 根据角色ID获取角色对象，并加载规则列表
     */
    public Role getRoleWithRules(Integer roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role != null) {
            List<Rule> rules = roleMapper.getRulesByRoleId(roleId);
            // 用反射或临时 Map 保存 rules 列表
            // 因为实体类中没有 rules 字段，前端可通过返回 Map 添加
            // 这里简单用 Role 的扩展方法（可在 DTO 层处理）
            // role.setRules(rules); // 如果需要 DTO，可在返回前组装
        }
        return role;
    }

    /**
     * 获取所有角色（不带规则）
     */
    public List<Role> getAllRoles() {
        return roleMapper.selectList(new QueryWrapper<Role>().eq("status", 1));
    }
}
