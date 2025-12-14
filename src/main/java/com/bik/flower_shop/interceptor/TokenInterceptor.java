package com.bik.flower_shop.interceptor;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Objects;

/**
 * @author bik
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    public TokenInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        // 简单日志，便于远端调试：打印方法、URI 和 token header（若存在）
        System.out.println("TokenInterceptor -> " + method + " " + uri + " Origin:" + request.getHeader("Origin") + " tokenHeader:" + request.getHeader("token"));

        // 允许预检请求通过（非常重要）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 如果是注册、登录等无需鉴权的接口，直接放行（兼容带 or 不带 /api 前缀的情况）
        // 你可以根据实际业务把这些路径集中到配置或常量中
        if (uri.endsWith("/user/register") || uri.endsWith("/user/login") || uri.contains("/admin/login")) {
            return true;
        }

        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            sendUnauthorized(response, "missing_token");
            return false;
        }

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

        Object account = tokenService.getByToken(token, role);
        if (account == null) {
            sendUnauthorized(response, "invalid_or_expired_token");
            return false;
        }

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
