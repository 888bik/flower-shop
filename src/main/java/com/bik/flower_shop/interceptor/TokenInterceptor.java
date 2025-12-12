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

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            sendUnauthorized(response, "missing_token");
            return false;
        }

        String role = "user";
        if (handler instanceof HandlerMethod method) {
            AuthRequired auth = method.getMethodAnnotation(AuthRequired.class);
            if (auth == null) {
                auth = method.getBeanType().getAnnotation(AuthRequired.class);
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
