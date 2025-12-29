package com.bik.flower_shop.service;

import com.bik.flower_shop.mapper.ChatSessionMapper;
import com.bik.flower_shop.pojo.entity.ChatSession;
import com.bik.flower_shop.utils.WsUserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 聊天会话服务
 *
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;

    public ChatSession getOrCreateSession(Integer userId, Integer serviceId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }

        // 1) 查询已有的会话（status = 1）
        ChatSession session = chatSessionMapper.selectByUserIdAndStatus(userId, 1);
        if (session != null) {
            // 如果已有会话但 serviceId 为 null，尝试分配一个在线客服并更新
            if (session.getServiceId() == null) {
                Integer assignServiceId = serviceId != null ? serviceId
                        : WsUserHolder.SERVICE.keySet().stream().findFirst().orElse(null);

                if (assignServiceId != null) {
                    session.setServiceId(assignServiceId);
                    session.setUpdateTime(now());
                    chatSessionMapper.update(session);
                } else {
                    // 没有在线客服，视业务可选择抛错或返回 session（但这里按之前逻辑抛错）
                    throw new RuntimeException("当前没有在线客服，请稍后再试");
                }
            }
            return session;
        }

        // 2) 没有会话 -> 分配客服
        Integer assignServiceId = serviceId != null ? serviceId
                : WsUserHolder.SERVICE.keySet().stream().findFirst().orElse(null);

        if (assignServiceId == null) {
            // 没有在线客服，返回异常
            throw new RuntimeException("当前没有在线客服，请稍后再试");
        }

        ChatSession newSession = new ChatSession();
        newSession.setUserId(userId);
        newSession.setServiceId(assignServiceId);
        newSession.setStatus(1);
        newSession.setCreateTime(now());
        newSession.setUpdateTime(now());

        // 会回填 newSession.id
        chatSessionMapper.insert(newSession);
        return newSession;
    }

    // 客服发消息时使用
    public ChatSession getSessionByServiceId(Integer serviceId) {
        return chatSessionMapper.selectByServiceId(serviceId);
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}

