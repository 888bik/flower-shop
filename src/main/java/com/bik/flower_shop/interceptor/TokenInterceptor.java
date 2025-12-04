package com.bik.flower_shop.interceptor;

import com.bik.flower_shop.exception.InvalidTokenException;
import com.bik.flower_shop.utils.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        try {
            TokenUtil.checkToken(token);  // 若 token 无效会抛出异常
            return true;
        } catch (InvalidTokenException e) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"msg\":\"" + e.getMessage() + "\",\"errorCode\":401}");
            return false;  // 拦截请求
        }
    }
}
