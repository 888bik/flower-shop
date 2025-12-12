package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.ManagerMapper;
import com.bik.flower_shop.mapper.RuleMapper;
import com.bik.flower_shop.pojo.dto.ManagerDTO;
import com.bik.flower_shop.pojo.dto.RoleSimpleDTO;
import com.bik.flower_shop.pojo.entity.Manager;
import com.bik.flower_shop.pojo.entity.Role;
import com.bik.flower_shop.pojo.entity.Rule;
import com.bik.flower_shop.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.bik.flower_shop.service.TokenService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerMapper managerMapper;
    private final RoleService roleService;
    private final RuleMapper ruleMapper;
    private final TokenService tokenService;

    @Transactional
    public Manager createManager(Manager manager) {
        // 基本校验 & trim
        if (manager.getUsername() == null || manager.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (manager.getPassword() == null || manager.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        manager.setUsername(manager.getUsername().trim());

        // 检查用户名是否存在
        long count = managerMapper.selectCount(new QueryWrapper<Manager>().eq("username", manager.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        manager.setPassword(PasswordUtil.encode(manager.getPassword().trim()));
        if (manager.getSuperAdmin() == null) {
            manager.setSuperAdmin(false);
        }
        if (manager.getStatus() == null) {
            manager.setStatus((byte) 1);
        }

        int now = (int) (System.currentTimeMillis() / 1000);
        manager.setCreateTime(now);
        manager.setUpdateTime(now);

        managerMapper.insert(manager);
        manager.setPassword(null);
        log.info("createManager success, id={}, username={}", manager.getId(), manager.getUsername());
        return manager;
    }

    public boolean updateManagerById(Integer id, Manager dto) {
        Manager old = managerMapper.selectById(id);
        if (old == null) {
            throw new BusinessException("该id不存在");
        }
        ;
        if (Boolean.TRUE.equals(old.getSuperAdmin())) {
            throw new BusinessException("超级管理员禁止修改");
        }

        if (dto.getUsername() != null && !dto.getUsername().equals(old.getUsername())) {
            long c = managerMapper.selectCount(new QueryWrapper<Manager>().eq("username", dto.getUsername()).ne("id", id));
            if (c > 0) {
                throw new BusinessException("用户名已存在");
            }
        }

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

    public Map<String, Object> getManagerList(Integer page, Integer limit, String keyword) {
        int offset = (page - 1) * limit;
        String kw = keyword == null ? "" : keyword;

        List<ManagerDTO> list = managerMapper.getManagerList(offset, limit, kw);

        int totalCount = managerMapper.getManagerCount(kw);

        List<RoleSimpleDTO> roles = managerMapper.getAllRoles();

        // 构建 manager.role 对象
        for (ManagerDTO m : list) {
            m.buildRole();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("totalCount", totalCount);
        data.put("roles", roles);
        return data;
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
        if (username == null || password == null) {
            throw new BusinessException("用户名或密码不能为空");
        }
        Manager manager = managerMapper.selectOne(new QueryWrapper<Manager>().eq("username", username.trim()));
        if (manager == null || !PasswordUtil.verify(password, manager.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        managerMapper.updateById(manager);

        manager.setPassword(null);

        return tokenService.createToken(manager, "admin");
    }

    // 退出登录
    public void logout(String token) {
        tokenService.invalidateToken(token, "admin");
    }

    // 修改当前登录用户密码（通过 token 获取 user）
    public boolean updatePasswordByToken(String token, String oldPassword, String newPassword, String rePassword) {
        Manager mgrFromRedis = tokenService.getManagerByToken(token);
        if (mgrFromRedis == null) {
            throw new RuntimeException("非法token，请先登录！");
        }

        Integer uid = mgrFromRedis.getId();
        Manager m = managerMapper.selectById(uid);
        if (m == null) {
            throw new RuntimeException("用户不存在");
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
        tokenService.invalidateToken(token, "admin");
        return true;
    }


    public Map<String, Object> getInfoByToken(String token) {
        Manager mgrFromRedis = tokenService.getManagerByToken(token);
        if (mgrFromRedis == null) {
            throw new RuntimeException("非法token，请先登录！");
        }
        // 使用 redis 中的 id 去数据库拉取最新信息（例如角色可能已变）
        Integer uid = mgrFromRedis.getId();
        Manager m = managerMapper.selectById(uid);
        if (m == null) {
            throw new RuntimeException("用户不存在");
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
            boolean isMenu = r.getMenu() != null && r.getMenu() == 1;
            if (Objects.equals(r.getRuleId(), parentId) && isMenu) {
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
