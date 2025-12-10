package com.bik.flower_shop.interceptor;

import com.bik.flower_shop.pojo.entity.Manager;
import com.bik.flower_shop.service.impl.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 全局 token 校验拦截器（读取请求头 token）
 * - token 放在请求头名 "token"（与你 Controller 保持一致）
 * - 校验失败返回 401 并中断请求
 * - 校验通过后把 Manager 放入 request attribute: "currentManager"
 *
 * @author bik
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(TokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 1. 从 header 读取 token
        String token = request.getHeader("token");

        // 2. token 为空或无效 -> 返回 401
        if (token == null || token.isBlank()) {
            sendUnauthorized(response, "missing_token");
            return false;
        }

        // 3. 去 Redis 校验 token
        Manager manager = tokenService.getManagerByToken(token);
        if (manager == null) {
            sendUnauthorized(response, "invalid_or_expired_token");
            return false;
        }

        // 4. 校验通过，把 manager 放入 request attribute（供 Controller/Service 使用）
        request.setAttribute("currentManager", manager);

        return true;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        // 你项目里有 ApiResult 类，这里返回简单 json，或改为 ApiResult.fail(...)
        String json = String.format("{\"code\":401, \"message\":\"%s\"}", message);
        response.getWriter().write(json);
    }
}
