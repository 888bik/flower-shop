package com.bik.flower_shop.mapper;

import com.bik.flower_shop.pojo.dto.ChatSessionDTO;
import com.bik.flower_shop.pojo.entity.ChatSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface ChatSessionMapper {

    @Select("SELECT * FROM chat_session WHERE user_id = #{userId} AND status = #{status} LIMIT 1")
    ChatSession selectByUserIdAndStatus(
            @Param("userId") Integer userId,
            @Param("status") Integer status
    );

    @Select("SELECT * FROM chat_session WHERE service_id = #{serviceId} AND status = 1 LIMIT 1")
    ChatSession selectByServiceId(@Param("serviceId") Integer serviceId);

    @Insert("INSERT INTO chat_session (user_id, service_id, status, create_time, update_time) VALUES (#{userId}, #{serviceId}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatSession session);

    @Update("UPDATE chat_session SET user_id = #{userId}, service_id = #{serviceId}, status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int update(ChatSession session);

    /**
     * 获取管理员客服的会话列表，同时返回用户信息
     */
    @Select("SELECT s.id AS id, s.user_id AS userId, s.service_id AS serviceId, s.status AS status, " +
            "s.create_time AS createTime, s.update_time AS updateTime, " +
            "u.nickname AS nickname, u.avatar AS avatar " +
            "FROM chat_session s " +
            "LEFT JOIN user u ON s.user_id = u.id " +
            "WHERE s.service_id = #{serviceId}")
    List<ChatSessionDTO> getSessionsByServiceId(@Param("serviceId") Integer serviceId);


    @Select("""
                SELECT cs.id, cs.user_id, cs.service_id, cs.status,
                       u.nickname, u.avatar
                FROM chat_session cs
                JOIN user u ON cs.user_id = u.id
                WHERE cs.user_id = #{userId}
                LIMIT 1
            """)
    ChatSessionDTO getUserSession(Integer userId);

}

