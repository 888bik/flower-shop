package com.bik.flower_shop.service.impl;

import com.bik.flower_shop.pojo.entity.Manager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * @author bik
 */
@Service
public class TokenService {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    private static final String TOKEN_PREFIX = "mgr:token:";
    private static final long TOKEN_TTL_SECONDS =3 * 60 * 60;

    public TokenService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成 token 并把 Manager（脱敏后的）信息保存到 Redis
     */
    public String createToken(Manager manager) {
        String token = UUID.randomUUID().toString().replace("-", "");
        try {
            // 注意：不要把敏感字段（如 password）写入 Redis 。示例中我们假设 manager 对象里不含明文密码或已脱敏。
            String json = objectMapper.writeValueAsString(manager);
            redis.opsForValue().set(TOKEN_PREFIX + token, json, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化 Manager 失败", e);
        }
        return token;
    }

    /**
     * 根据 token 获取 Manager 对象，若不存在返回 null。
     * 同时实现滑动过期：刷新 TTL。
     */
    public Manager getManagerByToken(String token) {
        if (token == null || token.isBlank()) return null;
        String key = TOKEN_PREFIX + token;
        String json = redis.opsForValue().get(key);
        if (json == null) return null;
        try {
            // 刷新 TTL（滑动过期）
            redis.expire(key, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
            return objectMapper.readValue(json, Manager.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 使 token 失效（登出/强制下线）
     */
    public void invalidateToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        redis.delete(TOKEN_PREFIX + token);
    }

    /**
     * 判断 token 是否存在
     */
    public boolean tokenExists(String token) {
        return redis.hasKey(TOKEN_PREFIX + token);
    }
}
