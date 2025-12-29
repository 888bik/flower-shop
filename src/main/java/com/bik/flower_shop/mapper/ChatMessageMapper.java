package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.dto.ChatMessageDTO;
import com.bik.flower_shop.pojo.entity.ChatMessage;
import com.bik.flower_shop.pojo.entity.ChatSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    @Insert("INSERT INTO chat_message (session_id, sender_id, sender_role, content, create_time) " +
            "VALUES (#{message.sessionId}, #{message.senderId}, #{message.senderRole}, #{message.content}, UNIX_TIMESTAMP())")
    int insert(@Param("message") ChatMessage message);


    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<ChatMessage> getMessagesBySessionId(Integer sessionId);


    /**
     * 查询某个会话的消息（用户消息 join 用户表，管理员消息使用后台当前管理员）
     */
    @Select("SELECT m.id, m.session_id, m.sender_id, m.sender_role, m.content, m.create_time, " +
            "u.nickname AS nickname, u.avatar AS avatar " +
            "FROM chat_message m " +
            "LEFT JOIN user u ON m.sender_id = u.id AND m.sender_role = 1 " +
            "WHERE m.session_id = #{sessionId} " +
            "ORDER BY m.create_time")
    List<ChatMessageDTO> getMessagesWithUserInfo(@Param("sessionId") Integer sessionId);
}
