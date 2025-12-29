package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.context.BaseController;
import com.bik.flower_shop.mapper.ChatMessageMapper;
import com.bik.flower_shop.mapper.ChatSessionMapper;
import com.bik.flower_shop.pojo.dto.ChatMessageDTO;
import com.bik.flower_shop.pojo.dto.ChatSessionDTO;
import com.bik.flower_shop.pojo.entity.ChatMessage;
import com.bik.flower_shop.pojo.entity.ChatSession;
import com.bik.flower_shop.pojo.entity.Manager;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@AuthRequired(role = "admin")
@RequestMapping("/admin/chat")
public class ChatAdminController extends BaseController {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final TokenService tokenService;

    @Override
    protected TokenService getTokenService() {
        return tokenService;
    }



    // 获取客服的会话列表
    @GetMapping("/sessions")
    public ApiResult<List<ChatSessionDTO>> getSessions(@RequestParam Integer serviceId) {
        Manager currentManager = getCurrentManager();
        return ApiResult.ok(chatSessionMapper.getSessionsByServiceId(serviceId));
    }

    // 获取某个会话的历史消息
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResult<List<ChatMessageDTO>> getMessages(@PathVariable Integer sessionId) {

        Manager currentManager = getCurrentManager();

        List<ChatMessageDTO> messages = chatMessageMapper.getMessagesWithUserInfo(sessionId);

        // 遍历消息，把管理员消息填充当前管理员信息
        messages.forEach(msg -> {
            if (msg.getSenderRole() == 2) { // 管理员消息
                msg.setNickname(currentManager.getUsername());
                msg.setAvatar(currentManager.getAvatar());
            } else { // 用户消息
                msg.setNickname(msg.getNickname() == null ? "用户" : msg.getNickname());
                msg.setAvatar(msg.getAvatar() == null ? "" : msg.getAvatar());
            }
        });

        return ApiResult.ok(messages);
    }
}
