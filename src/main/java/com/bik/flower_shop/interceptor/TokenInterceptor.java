package com.bik.flower_shop.interceptor;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * TokenInterceptor
 * - 只校验 accessToken
 * - refresh 接口直接放行
 *
 * @author bik
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    public TokenInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 1. 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 2.白名单接口
        if (
                uri.endsWith("/user/register")
                        || uri.endsWith("/user/login")
                        || uri.contains("/admin/login")
                        || uri.contains("/auth/refresh")
        ) {
            return true;
        }

        //3.读取 accessToken
        String token = request.getHeader("accessToken");
        if (token == null || token.isBlank()) {
            sendUnauthorized(response, "missing_access_token");
            return false;
        }

        //4.解析角色
        String role = "user";
        if (handler instanceof HandlerMethod methodHandler) {
            AuthRequired auth = methodHandler.getMethodAnnotation(AuthRequired.class);
            if (auth == null) {
                auth = methodHandler.getBeanType().getAnnotation(AuthRequired.class);
            }
            if (auth != null) {
                role = auth.role();
            }
        }

        //5. 校验 accessToke
        Object account = tokenService.getByAccessToken(token, role, Object.class);
        if (account == null) {
            sendUnauthorized(response, "invalid_or_expired_access_token");
            return false;
        }

        //6. 注入当前用户
        if ("admin".equals(role)) {
            request.setAttribute("currentManager", account);
        } else {
            request.setAttribute("currentUser", account);
        }

        return true;
    }

    private void sendUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + msg + "\"}");
    }
}
