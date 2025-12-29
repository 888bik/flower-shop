package com.bik.flower_shop.service;

import com.bik.flower_shop.mapper.ChatMessageMapper;
import com.bik.flower_shop.pojo.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService {


    private final ChatMessageMapper chatMessageMapper;

    public ChatMessage saveMessage(Integer sessionId, Integer senderRole, Integer senderId, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderRole(senderRole);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setCreateTime(System.currentTimeMillis() / 1000L);

        chatMessageMapper.insert(msg);
        return msg;
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}
