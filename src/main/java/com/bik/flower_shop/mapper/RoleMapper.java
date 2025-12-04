package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 获取角色及其所有规则ID列表
     */
    @Select("SELECT r.* FROM rule r " +
            "INNER JOIN role_rule rr ON r.id = rr.rule_id " +
            "WHERE rr.role_id = #{roleId} AND r.status = 1 " +
            "ORDER BY r.`order`, r.id")
    List<com.bik.flower_shop.pojo.entity.Rule> getRulesByRoleId(Integer roleId);
}
