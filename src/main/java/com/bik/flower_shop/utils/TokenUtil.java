package com.bik.flower_shop.utils;

import com.bik.flower_shop.exception.InvalidTokenException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenUtil {

    private static final Map<String, Integer> LOGIN_STATE = new ConcurrentHashMap<>();

    public static String createToken(Integer userId) {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void storeLoginState(String token, Integer userId) {
        LOGIN_STATE.put(token, userId);
    }

    public static Integer getUserId(String token) {
        return LOGIN_STATE.get(token);
    }

    public static void removeLoginState(String token) {
        LOGIN_STATE.remove(token);
    }

    // 检查 token 是否有效，若无效抛出 InvalidTokenException
    public static Integer checkToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException("非法token，请先登录！");
        }
        Integer uid = getUserId(token);
        if (uid == null) {
            throw new InvalidTokenException("非法token，请先登录！");
        }
        return uid;
    }

    public static void logout(String token) {
        removeLoginState(token);
    }
}
