package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.mapper.ManagerMapper;
import com.bik.flower_shop.mapper.RuleMapper;
import com.bik.flower_shop.pojo.entity.Manager;
import com.bik.flower_shop.pojo.entity.Role;
import com.bik.flower_shop.pojo.entity.Rule;
import com.bik.flower_shop.utils.PasswordUtil;
import com.bik.flower_shop.utils.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerMapper managerMapper;
    private final RoleService roleService;
    private final RuleMapper ruleMapper;

    // 获取管理员分页列表（不含 role.rules，role 只返回 id/name）
    public Page<Manager> getManagerList(long page, long limit, String keyword) {
        Page<Manager> pager = new Page<>(page, limit);
        QueryWrapper<Manager> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            query.like("username", keyword);
        }
        query.orderByDesc("id");
        return managerMapper.selectPage(pager, query);
    }

    // 返回 list + totalCount + roles（供 Controller 直接使用）
    public Map<String, Object> getManagerListWithRoles(long page, long limit, String keyword) {
        Page<Manager> p = getManagerList(page, limit, keyword);
        List<Role> roles = roleService.getAllRoles();
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("totalCount", p.getTotal());
        result.put("roles", roles);
        return result;
    }

    // 新增管理员（检查 username 唯一）
    public Manager createManager(Manager manager) {
        // 校验密码
        if (manager.getPassword() == null || manager.getPassword().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        // 设置默认非超级管理员
        if (manager.getSuperAdmin() == null) {
            manager.setSuperAdmin(false);
        }

        // 检查用户名是否存在
        long count = managerMapper.selectCount(
                new QueryWrapper<Manager>().eq("username", manager.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 密码加密
        manager.setPassword(PasswordUtil.encode(manager.getPassword()));

        // 默认状态
        if (manager.getStatus() == null) {
            manager.setStatus((byte) 1);
        }

        int now = (int) (System.currentTimeMillis() / 1000);
        manager.setCreateTime(now);
        manager.setUpdateTime(now);

        // 插入数据库
        managerMapper.insert(manager);

        // 不返回密码
        manager.setPassword(null);
        return manager;
    }

    // 更新管理员（检查 username 唯一）
    public boolean updateManagerById(Integer id, Manager dto) {
        Manager old = managerMapper.selectById(id);
        if (old == null) {
            throw new RuntimeException("该id不存在");
        }
        if (old.getSuperAdmin() != null && old.getSuperAdmin()) {
            throw new RuntimeException("超级管理员禁止修改");
        }
        // 若改用户名，检查重复
        if (dto.getUsername() != null && !dto.getUsername().equals(old.getUsername())) {
            Integer c = Math.toIntExact(managerMapper.selectCount(new QueryWrapper<Manager>().eq("username", dto.getUsername()).ne("id", id)));
        }
        // 如果密码为空字符串则忽略，如果不为空则加密
        if (dto.getPassword() != null) {
            if (dto.getPassword().isEmpty()) {
                dto.setPassword(null);
            } else {
                dto.setPassword(PasswordUtil.encode(dto.getPassword()));
            }
        }
        dto.setId(id);
        dto.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        managerMapper.updateById(dto);
        return true;
    }

    // 删除管理员
    public boolean deleteManager(Integer id) {
        Manager m = managerMapper.selectById(id);
        if (m == null) {
            throw new RuntimeException("该id不存在");
        }
        if (m.getSuperAdmin() != null && m.getSuperAdmin()) {
            throw new RuntimeException("超级管理员不能删除");
        }
        managerMapper.deleteById(id);
        return true;
    }

    // 修改状态
    public boolean updateStatus(Integer id, Integer status) {
        Manager m = managerMapper.selectById(id);
        if (m == null) {
            throw new RuntimeException("该id不存在");
        }
        if (m.getSuperAdmin() != null && m.getSuperAdmin()) {
            throw new RuntimeException("超级管理员禁止操作");
        }
        m.setStatus(status.byteValue());
        m.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        managerMapper.updateById(m);
        return true;
    }

    // 登录
    public String login(String username, String password) {
        Manager manager = managerMapper.selectOne(new QueryWrapper<Manager>().eq("username", username));
        if (manager == null || !PasswordUtil.verify(password, manager.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = TokenUtil.createToken(manager.getId());
        TokenUtil.storeLoginState(token, manager.getId());
        return token;
    }

    // 退出登录
    public void logout(String token) {
        TokenUtil.logout(token);
    }

    // 修改当前登录用户密码（通过 token 获取 user）
    public boolean updatePasswordByToken(String token, String oldPassword, String newPassword, String rePassword) {
        Integer uid = TokenUtil.checkToken(token);
        Manager m = managerMapper.selectById(uid);
        if (m == null) {
            throw new RuntimeException("非法token，请先登录！");
        }
        if (!PasswordUtil.verify(oldPassword, m.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }
        if (!Objects.equals(newPassword, rePassword)) {
            throw new RuntimeException("新密码和确认密码不一致");
        }
        m.setPassword(PasswordUtil.encode(newPassword));
        m.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        managerMapper.updateById(m);
        // 使当前 token 失效
        TokenUtil.logout(token);
        return true;
    }

    // 获取当前管理员信息（包含 role、menus、ruleNames）
    public Map<String, Object> getInfoByToken(String token) {
        Integer uid = TokenUtil.checkToken(token);
        Manager m = managerMapper.selectById(uid);
        if (m == null) {
            throw new RuntimeException("非法token，请先登录！");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", m.getId());
        result.put("username", m.getUsername());
        result.put("avatar", m.getAvatar());
        result.put("super", m.getSuperAdmin() != null && m.getSuperAdmin() ? 1 : 0);

        // 角色及规则
        Role role = null;
        List<Rule> rules = Collections.emptyList();
        if (m.getRoleId() != null) {
            role = roleService.getRoleById(m.getRoleId());
            if (role != null) {
                rules = ruleMapper.selectList(
                        new QueryWrapper<Rule>()
                                .inSql("id", "SELECT rule_id FROM role_rule WHERE role_id=" + m.getRoleId())
                                .eq("status", 1)
                                .orderByAsc("`order`", "id")
                );
            }
        }

        // 超级管理员获取所有规则
        if (m.getSuperAdmin() != null && m.getSuperAdmin()) {
            rules = ruleMapper.selectList(new QueryWrapper<Rule>().eq("status", 1).orderByAsc("`order`", "id"));
        }

        result.put("role", role);

        // 菜单树构建
        List<Map<String, Object>> menuTree = buildMenuTree(rules, 0);
        result.put("menus", menuTree);

        // ruleNames
        List<String> ruleNames = rules.stream()
                .filter(r -> r.getCondition() != null && !r.getCondition().isEmpty() && r.getName() != null)
                .map(r -> r.getCondition() + "," + (r.getMethod() == null ? "GET" : r.getMethod()))
                .collect(Collectors.toList());
        result.put("ruleNames", ruleNames);

        return result;
    }

    /**
     * 构建菜单树（返回 Map 对象，兼容前端）
     */
    private List<Map<String, Object>> buildMenuTree(List<Rule> rules, Integer parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Rule r : rules) {
            if (Objects.equals(r.getRuleId(), parentId) && r.getMenu() != null && r.getMenu()) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", r.getId());
                node.put("rule_id", r.getRuleId());
                node.put("status", r.getStatus());
                node.put("create_time", r.getCreateTime());
                node.put("update_time", r.getUpdateTime());
                node.put("name", r.getName());
                node.put("desc", r.getDescription());
                node.put("frontpath", r.getFrontpath());
                node.put("condition", r.getCondition());
                node.put("menu", r.getMenu());
                node.put("order", r.getOrder());
                node.put("icon", r.getIcon());
                node.put("method", r.getMethod());
                // 子节点递归
                List<Map<String, Object>> children = buildMenuTree(rules, r.getId());
                node.put("child", children);
                tree.add(node);
            }
        }
        // 按 order 排序
        tree.sort(Comparator.comparingInt(n -> (Integer) n.getOrDefault("order", 0)));
        return tree;
    }


    // 工具：把规则列表转为树（以 ruleId 作为父子关系）
    private List<Rule> listToTree(List<Rule> items, Integer rootId) {
        Map<Integer, Rule> idMap = new HashMap<>();
        for (Rule r : items) {
            idMap.put(r.getId(), r);
        }

        List<Rule> roots = new ArrayList<>();
        for (Rule r : items) {
            if (Objects.equals(r.getRuleId(), rootId)) {
                roots.add(r);
            } else {
                Rule parent = idMap.get(r.getRuleId());
                if (parent != null) {
                    if (parent.getChild() == null) {
                        parent.setChild(new ArrayList<>());
                    }
                    parent.getChild().add(r);
                }
            }
        }

        // 递归排序
        Comparator<Rule> cmp = Comparator.comparingInt(rr -> rr.getOrder() == null ? 0 : rr.getOrder());
        sortTree(roots, cmp);
        return roots;
    }

    private void sortTree(List<Rule> nodes, Comparator<Rule> cmp) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(cmp);
        for (Rule node : nodes) {
            if (node.getChild() != null && !node.getChild().isEmpty()) {
                sortTree(node.getChild(), cmp);
            }
        }
    }

}
