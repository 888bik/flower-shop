package com.bik.flower_shop.config;

import com.bik.flower_shop.handler.ChatWebSocketHandler;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.util.Map;

/**
 * WebSocket 配置，支持通过 query 参数传 token（Postman 可用）
 * @author bik
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final TokenService tokenService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request,
                                                   ServerHttpResponse response,
                                                   WebSocketHandler wsHandler,
                                                   Map<String, Object> attributes) throws Exception {
                        // 从 query 参数获取 accessToken
                        String token = null;
                        String query = request.getURI().getQuery();
                        if (query != null) {
                            for (String part : query.split("&")) {
                                if (part.startsWith("accessToken=")) {
                                    token = part.substring("accessToken=".length());
                                    break;
                                }
                            }
                        }

                        if (token != null && !token.isBlank()) {
                            // 只调用一次 tokenService
                            var user = tokenService.getUserByAccessToken(token);
                            var manager = tokenService.getManagerByAccessToken(token);

                            if (user != null) {
                                // 确保放入 Integer
                                Integer userId = Integer.valueOf(user.getId().toString());
                                attributes.put("userId", userId);
                            }
                            if (manager != null) {
                                Integer serviceId = Integer.valueOf(manager.getId().toString());
                                attributes.put("serviceId", serviceId);
                            }
                        }

                        // 调试日志（上线后可删）
                        System.out.println("WS beforeHandshake attributes=" + attributes);
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }
                })
                .setAllowedOrigins("*");
    }
}
