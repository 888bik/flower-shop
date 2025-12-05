package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.dto.RoleRulePivotDTO;
import com.bik.flower_shop.pojo.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 获取角色及其所有规则ID列表
     */
    @Select("""
                SELECT r.id AS ruleId, rr.id AS pivotId, rr.role_id AS roleId
                FROM rule r
                INNER JOIN role_rule rr ON r.id = rr.rule_id
                WHERE rr.role_id = #{roleId}
                ORDER BY r.`order`, r.id
            """)
    List<RoleRulePivotDTO> getRoleRules(Integer roleId);

    /**
     * 判断角色是否仍绑定规则
     */
    @Select("SELECT COUNT(*) FROM role_rule WHERE role_id = #{roleId}")
    Integer countRoleRules(Integer roleId);


    @Update("UPDATE role SET status = #{status} WHERE id = #{id}")
    int updateStatus(Integer id, Byte status);

    /**
     * 删除角色与规则的关联记录
     */
    @Delete("DELETE FROM role_rule WHERE role_id = #{roleId}")
    void deleteRoleRules(Integer roleId);
}
