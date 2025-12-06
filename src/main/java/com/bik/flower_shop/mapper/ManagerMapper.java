package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.dto.ManagerDTO;
import com.bik.flower_shop.pojo.dto.RoleDTO;
import com.bik.flower_shop.pojo.dto.RoleSimpleDTO;
import com.bik.flower_shop.pojo.entity.Manager;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ManagerMapper extends BaseMapper<Manager> {


    @Select("""
                SELECT m.*, r.id AS role_id, r.name AS role_name
                FROM manager m
                LEFT JOIN role r ON m.role_id = r.id
                WHERE m.username LIKE CONCAT('%', #{keyword}, '%')
                ORDER BY m.id DESC
                LIMIT #{offset}, #{limit}
            """)
    List<ManagerDTO> getManagerList(@Param("offset") int offset,
                                    @Param("limit") int limit,
                                    @Param("keyword") String keyword);

    @Select("SELECT id, name FROM role WHERE status = 1")
    List<RoleSimpleDTO> getAllRoles();

    @Select("""
                SELECT COUNT(*)
                FROM manager
                WHERE username LIKE CONCAT('%', #{keyword}, '%')
            """)
    int getManagerCount(@Param("keyword") String keyword);

}
