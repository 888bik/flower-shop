package com.bik.flower_shop.context;

import com.bik.flower_shop.pojo.entity.Manager;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.service.TokenService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 基础控制器：提供获取当前用户和 token 方法
 * @author bik
 */
public abstract class BaseController {

    protected abstract TokenService getTokenService();

    protected HttpServletRequest getRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return ((ServletRequestAttributes) attrs).getRequest();
    }

    /**
     * 获取当前登录用户（通过 accessToken）
     */
    protected User getCurrentUser() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String accessToken = request.getHeader("accessToken");
        if (accessToken == null || accessToken.isBlank()) {
            accessToken = request.getParameter("accessToken");
        }
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }

        return getTokenService().getUserByAccessToken(accessToken);
    }

    protected Manager getCurrentManager() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String accessToken = request.getHeader("accessToken");
        if (accessToken == null || accessToken.isBlank()) {
            accessToken = request.getParameter("accessToken");
        }
        return getTokenService().getManagerByAccessToken(accessToken);
    }


    /**
     * 获取当前用户 id
     */
    protected Integer getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取 accessToken
     */
    protected String getAccessToken() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String accessToken = request.getHeader("accessToken");
        if (accessToken == null || accessToken.isBlank()) {
            accessToken = request.getParameter("accessToken");
        }
        return accessToken;
    }

    /**
     * 获取 refreshToken
     */
    protected String getRefreshToken() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String refreshToken = request.getHeader("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshToken = request.getParameter("refreshToken");
        }
        return refreshToken;
    }
}
