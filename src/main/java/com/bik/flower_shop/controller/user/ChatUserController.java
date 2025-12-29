package com.bik.flower_shop.controller.user;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.context.BaseController;
import com.bik.flower_shop.mapper.ChatMessageMapper;
import com.bik.flower_shop.mapper.ChatSessionMapper;
import com.bik.flower_shop.pojo.dto.ChatMessageDTO;
import com.bik.flower_shop.pojo.dto.ChatSessionDTO;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.service.ChatUserService;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/chat")
public class ChatUserController extends BaseController {

    private final TokenService tokenService;

    @Override
    protected TokenService getTokenService() {
        return tokenService;
    }

    private final ChatUserService chatUserService;

    /**
     * 获取当前用户的客服会话（没有就创建）
     */
    @GetMapping("/session")
    public ApiResult<ChatSessionDTO> getSession() {
        User user = getCurrentUser();
        return ApiResult.ok(
                chatUserService.getOrCreateSession(user.getId())
        );
    }


    /**
     * 获取当前会话的历史消息
     */
    @GetMapping("/session/{sessionId}/messages")
    public ApiResult<List<ChatMessageDTO>> getMessages(
            @PathVariable Integer sessionId) {

        User user = getCurrentUser();

        return ApiResult.ok(
                chatUserService.getUserMessages(user.getId(), sessionId)
        );
    }
}
