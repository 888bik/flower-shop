package com.bik.flower_shop.service;

import com.bik.flower_shop.mapper.ChatMessageMapper;
import com.bik.flower_shop.mapper.ChatSessionMapper;
import com.bik.flower_shop.pojo.dto.ChatMessageDTO;
import com.bik.flower_shop.pojo.dto.ChatSessionDTO;
import com.bik.flower_shop.pojo.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class ChatUserService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;

    /**
     * 获取或创建用户会话
     */
    public ChatSessionDTO getOrCreateSession(Integer userId) {

        // 1. 先查
        ChatSessionDTO session = chatSessionMapper.getUserSession(userId);
        if (session != null) {
            return session;
        }

        // 2. 构造会话对象
        ChatSession chatSession = new ChatSession();
        chatSession.setUserId(userId);
        // 后期可换成分配策略
        chatSession.setServiceId(3);
        chatSession.setStatus(1);

        int now = (int) (System.currentTimeMillis() / 1000);
        chatSession.setCreateTime(now);
        chatSession.setUpdateTime(now);

        // 3. 使用【通用 insert】
        chatSessionMapper.insert(chatSession);

        // 4. 再查一次（拿 DTO + 用户信息）
        return chatSessionMapper.getUserSession(userId);
    }

    /**
     * 获取用户自己的聊天记录
     */
    public List<ChatMessageDTO> getUserMessages(Integer userId, Integer sessionId) {

        // 校验 session 属主
        ChatSessionDTO session = chatSessionMapper.getUserSession(userId);
        if (session == null || !session.getId().equals(sessionId)) {
            throw new RuntimeException("非法会话访问");
        }

        return chatMessageMapper.getMessagesWithUserInfo(sessionId);
    }
}
