package com.bik.flower_shop.context;

import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.service.TokenService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
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

    protected User getCurrentUser() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            token = request.getParameter("token");
        }
        if (token == null || token.isBlank()) {
            return null;
        }


        return getTokenService().getUserByToken(token);
    }

    protected Integer getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
}
