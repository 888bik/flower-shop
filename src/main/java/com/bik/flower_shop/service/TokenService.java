package com.bik.flower_shop.service;

import com.bik.flower_shop.pojo.entity.Manager;
import com.bik.flower_shop.pojo.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * TokenService
 * - accessToken + refreshToken
 * - Redis 存储
 * - 支持多角色（admin / user）
 *
 * @author bik
 */
@Service
public class TokenService {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    /**
     * accessToken 有效期：30 分钟
     */
    private static final long ACCESS_TOKEN_TTL = 60 * 60 * 3;

    /**
     * refreshToken 有效期：7 天
     */
    private static final long REFRESH_TOKEN_TTL = 7 * 24 * 60 * 60;

    private static final Map<String, String> ACCESS_PREFIX = Map.of(
            "admin", "admin:access:",
            "user", "user:access:"
    );

    private static final Map<String, String> REFRESH_PREFIX = Map.of(
            "admin", "admin:refresh:",
            "user", "user:refresh:"
    );

    public TokenService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }


    /**
     * 创建 accessToken + refreshToken
     */
    public Map<String, String> createTokenPair(Object account, String role) {
        String accessToken = genToken();
        String refreshToken = genToken();

        try {
            String json = objectMapper.writeValueAsString(account);

            redis.opsForValue().set(
                    accessKey(role, accessToken),
                    json,
                    ACCESS_TOKEN_TTL,
                    TimeUnit.SECONDS
            );

            redis.opsForValue().set(
                    refreshKey(role, refreshToken),
                    json,
                    REFRESH_TOKEN_TTL,
                    TimeUnit.SECONDS
            );

            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            );
        } catch (Exception e) {
            throw new RuntimeException("Token serialize error", e);
        }
    }

    //accessToken 校验
    public <T> T getByAccessToken(String token, String role, Class<T> clazz) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String json = redis.opsForValue().get(accessKey(role, token));
        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * refreshToken → 新 accessToken + refreshToken（旋转）
     */
    public Map<String, String> refreshByRefreshToken(String refreshToken, String role) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }

        String key = refreshKey(role, refreshToken);
        String json = redis.opsForValue().get(key);
        if (json == null) {
            return null;
        }

        try {
            // refreshToken 只能用一次（旋转）
            redis.delete(key);

            Object account = objectMapper.readValue(json, Object.class);
            return createTokenPair(account, role);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 根据用户 ID 注销该用户所有 token（access + refresh）
     */
    public void invalidateAllByManager(String userId, String role) {
        // 删除所有 accessToken
        Set<String> accessKeys = redis.keys(ACCESS_PREFIX.get(role) + "*");
        for (String key : accessKeys) {
            String json = redis.opsForValue().get(key);
            // 判断是否是当前用户
            if (json != null && json.contains(userId)) {
                redis.delete(key);
            }
        }

        // 删除所有 refreshToken
        Set<String> refreshKeys = redis.keys(REFRESH_PREFIX.get(role) + "*");
        for (String key : refreshKeys) {
            String json = redis.opsForValue().get(key);
            if (json != null && json.contains(userId)) {
                redis.delete(key);
            }
        }
    }


    /**
     * 仅失效 accessToken
     */
    public void invalidateAccessToken(String token, String role) {
        if (token == null) {
            return;
        }
        redis.delete(accessKey(role, token));
    }

    /**
     * 仅失效 refreshToken
     */
    public void invalidateRefreshToken(String token, String role) {
        if (token == null) {
            return;
        }
        redis.delete(refreshKey(role, token));
    }

    /**
     * 同时失效 access + refresh
     */
    public void invalidateAll(String accessToken, String refreshToken, String role) {
        invalidateAccessToken(accessToken, role);
        invalidateRefreshToken(refreshToken, role);
    }

    public Manager getManagerByAccessToken(String token) {
        return getByAccessToken(token, "admin", Manager.class);
    }

    public User getUserByAccessToken(String token) {
        return getByAccessToken(token, "user", User.class);
    }


    private String genToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String accessKey(String role, String token) {
        return ACCESS_PREFIX.get(role) + token;
    }

    private String refreshKey(String role, String token) {
        return REFRESH_PREFIX.get(role) + token;
    }
}
