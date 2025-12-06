package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.mapper.RoleMapper;
import com.bik.flower_shop.mapper.RoleRuleMapper;
import com.bik.flower_shop.pojo.dto.RoleDTO;
import com.bik.flower_shop.pojo.dto.RoleRulePivotDTO;
import com.bik.flower_shop.pojo.entity.Role;
import com.bik.flower_shop.pojo.entity.RoleRule;
import com.bik.flower_shop.pojo.entity.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RoleRuleMapper roleRuleMapper;

    /**
     * 根据角色ID获取角色对象（不包含规则）
     */
    public Role getRoleById(Integer roleId) {
        return roleMapper.selectById(roleId);
    }

    /**
     * Description:创建角色
     *
     * @param dto
     * @return com.bik.flower_shop.pojo.entity.Role
     * @date 2025/12/5 17:00
     */
    public Role createRole(RoleDTO dto) {
        Role role = new Role();

        role.setName(dto.getName());
        role.setDescription(dto.getDescription());

        role.setStatus(dto.getStatus() != null ? dto.getStatus() : (byte) 1);

        int now = (int) Instant.now().getEpochSecond();
        role.setCreateTime(now);
        role.setUpdateTime(now);

        roleMapper.insert(role);
        return role;
    }

    /**
     * Description:更新角色
     *
     * @param roleId
     * @param dto
     * @return boolean
     * @date 2025/12/5 17:01
     */
    @Transactional
    public boolean updateRole(Integer roleId, RoleDTO dto) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return false;
        }

        role.setName(dto.getName());
        role.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }

        role.setUpdateTime((int) Instant.now().getEpochSecond());

        return roleMapper.updateById(role) > 0;
    }

    /**
     * Description:获取角色列表
     *
     * @param page
     * @param pageSize
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @date 2025/12/5 17:01
     */
    public Map<String, Object> getRoleList(Integer page, Integer pageSize) {

        // 分页
        Page<Role> p = new Page<>(page, pageSize);
        Page<Role> result = roleMapper.selectPage(
                p,
                new QueryWrapper<Role>().orderByDesc("id")
        );

        List<Object> list = new ArrayList<>();

        for (Role role : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", role.getId());
            map.put("name", role.getName());
            map.put("desc", role.getDescription());
            map.put("status", role.getStatus());
            map.put("create_time", role.getCreateTime());
            map.put("update_time", role.getUpdateTime());

            // 查询规则
            List<RoleRulePivotDTO> rules = roleMapper.getRoleRules(role.getId());
            List<Object> ruleList = new ArrayList<>();

            for (RoleRulePivotDTO item : rules) {
                Map<String, Object> ruleMap = new HashMap<>();
                ruleMap.put("id", item.getRuleId());

                Map<String, Object> pivot = new HashMap<>();
                pivot.put("id", item.getPivotId());
                pivot.put("role_id", item.getRoleId());
                pivot.put("rule_id", item.getRuleId());

                ruleMap.put("pivot", pivot);
                ruleList.add(ruleMap);
            }

            map.put("rules", ruleList);
            list.add(map);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("totalCount", result.getTotal());
        return data;
    }


    @Transactional
    public Boolean deleteRole(Integer roleId) {
        // 删除角色权限绑定，避免脏数据
        roleMapper.deleteRoleRules(roleId);
        // 执行删除
        return roleMapper.deleteById(roleId) > 0;
    }

    @Transactional
    public Boolean updateStatus(Integer id, Byte status) {
        return roleMapper.updateStatus(id, status) > 0;
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean setRoleRules(Integer roleId, List<Integer> ruleIds) {
        if (roleId == null) {
            throw new IllegalArgumentException("roleId 不能为空");
        }

        // 1. 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 2. 删除旧关联（role_rule）
        roleMapper.deleteRoleRules(roleId);

        // 3. 如果为空表示清空权限
        if (ruleIds == null || ruleIds.isEmpty()) {
            return true;
        }

        // 4. 逐条 insert（简单可靠，避免注解脚本问题）
        for (Integer rid : ruleIds) {
            if (rid == null) {
                continue;
            }
            RoleRule rr = new RoleRule();
            rr.setRoleId(roleId);
            rr.setRuleId(rid);
            roleRuleMapper.insert(rr);
        }

        return true;
    }


    /**
     * 获取所有启用角色
     */
    public List<Role> getAllRoles() {
        return roleMapper.selectList(new QueryWrapper<Role>().eq("status", 1));
    }
}
