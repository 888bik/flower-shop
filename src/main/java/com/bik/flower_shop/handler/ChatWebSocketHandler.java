package com.bik.flower_shop.handler;

import com.alibaba.fastjson.JSON;
import com.bik.flower_shop.pojo.dto.WsChatMessageDTO;
import com.bik.flower_shop.pojo.entity.ChatMessage;
import com.bik.flower_shop.pojo.entity.ChatSession;
import com.bik.flower_shop.service.ChatMessageService;
import com.bik.flower_shop.service.ChatSessionService;
import com.bik.flower_shop.utils.WsUserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * @author bik
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Integer userId = getUserId(session);
        Integer serviceId = getServiceId(session);

        if (userId != null) {
            WsUserHolder.USER.put(userId, session);
            log.info("用户上线 userId={}", userId);
        } else if (serviceId != null) {
            WsUserHolder.SERVICE.put(serviceId, session);
            log.info("客服上线 serviceId={}", serviceId);
        } else {
            log.warn("WebSocket session 未包含 userId 或 serviceId, sessionId={}", session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WsChatMessageDTO dto = JSON.parseObject(message.getPayload(), WsChatMessageDTO.class);
        if ("chat".equals(dto.getType())) {
            handleChat(session, dto);
        }
    }

    private void handleChat(WebSocketSession session, WsChatMessageDTO dto) throws Exception {
        Integer userId = getUserId(session);
        Integer serviceId = getServiceId(session);
        boolean fromUser = userId != null;

        ChatSession chatSession;

        if (fromUser) {
            // 用户发消息：找或创建会话
            chatSession = chatSessionService.getOrCreateSession(userId, null);
            if (chatSession.getServiceId() == null) {
                // 没有在线客服
                session.sendMessage(new TextMessage(JSON.toJSONString(
                        "当前没有在线客服，请稍后再试"
                )));
                return;
            }
        } else {
            // 客服发消息：直接找到该客服负责的用户会话
            chatSession = chatSessionService.getSessionByServiceId(serviceId);
            if (chatSession == null) {
                session.sendMessage(new TextMessage(JSON.toJSONString(
                        "没有用户正在与您会话"
                )));
                return;
            }
        }

        // 保存消息
        ChatMessage msg = chatMessageService.saveMessage(
                chatSession.getId(),
                fromUser ? 1 : 2,
                fromUser ? userId : serviceId,
                dto.getContent()
        );

        // 推送给对方
        Integer targetId = fromUser ? chatSession.getServiceId() : chatSession.getUserId();
        if (targetId == null) {
            log.warn("目标用户/客服未在线，消息暂不发送, chatSessionId={}", chatSession.getId());
            return;
        }

        WebSocketSession targetSession = fromUser
                ? WsUserHolder.SERVICE.get(targetId)
                : WsUserHolder.USER.get(targetId);

        if (targetSession == null || !targetSession.isOpen()) {
            log.warn("目标用户/客服未在线，消息暂不发送, targetId={}", targetId);
            return;
        }

        targetSession.sendMessage(new TextMessage(JSON.toJSONString(msg)));
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = getUserId(session);
        Integer serviceId = getServiceId(session);

        if (userId != null) {
            WsUserHolder.USER.remove(userId);
            log.info("用户下线 userId={}", userId);
        }
        if (serviceId != null) {
            WsUserHolder.SERVICE.remove(serviceId);
            log.info("客服下线 serviceId={}", serviceId);
        }
    }

    private Integer getUserId(WebSocketSession session) {
        Object val = session.getAttributes().get("userId");
        return val instanceof Integer ? (Integer) val : null;
    }

    private Integer getServiceId(WebSocketSession session) {
        Object val = session.getAttributes().get("serviceId");
        return val instanceof Integer ? (Integer) val : null;
    }
}
