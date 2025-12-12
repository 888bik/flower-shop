package com.bik.flower_shop.service;

import com.bik.flower_shop.pojo.entity.Manager;
import com.bik.flower_shop.pojo.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * TokenService - 支持多角色 token（admin / user 等）
 * @author bik
 */
@Service
public class TokenService {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private static final long TOKEN_TTL_SECONDS = 6 * 60 * 60;

    // 可扩展的前缀映射，方便未来增加其它角色
    private static final Map<String, String> PREFIX_MAP = new ConcurrentHashMap<>();
    static {
        PREFIX_MAP.put("admin", "mgr:token:");
        PREFIX_MAP.put("user", "user:token:");
    }

    public TokenService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    private String getPrefix(String role) {
        return PREFIX_MAP.getOrDefault(role, role + ":token:");
    }

    /**
     * 创建 token 并把序列化后的 account 保存到 Redis（带前缀区分角色）
     * @param account 管理员或用户对象（已脱敏）
     * @param role 'admin' 或 'user' 等
     * @return token 字符串
     */
    public String createToken(Object account, String role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String prefix = getPrefix(role);
        try {
            String json = objectMapper.writeValueAsString(account);
            redis.opsForValue().set(prefix + token, json, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }
        return token;
    }

    /**
     * 类型安全的读取 token -> 反序列化为指定类型
     * @param token token 字符串
     * @param role 角色 'admin' / 'user'
     * @param clazz 想要反序列化成的类型，如 Manager.class
     * @param <T> 返回类型
     * @return 反序列化对象或 null（不存在或反序列化失败）
     */
    public <T> T getByToken(String token, String role, Class<T> clazz) {
        if (token == null || token.isBlank()) return null;
        String prefix = getPrefix(role);
        String key = prefix + token;
        String json = redis.opsForValue().get(key);
        if (json == null) return null;
        try {
            // 滑动过期：刷新 TTL
            redis.expire(key, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 兼容旧代码：非泛型版（会返回 Manager 或 User 对象），推荐使用泛型版。
     */
    public Object getByToken(String token, String role) {
        if ("admin".equals(role)) {
            return getByToken(token, role, Manager.class);
        } else {
            return getByToken(token, role, User.class);
        }
    }

    public boolean tokenExists(String token, String role) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return redis.hasKey(getPrefix(role) + token);
    }

    public void invalidateToken(String token, String role) {
        if (token == null || token.isBlank()) {
            return;
        }
        redis.delete(getPrefix(role) + token);
    }

    // 辅助方法（更语义化）
    public Manager getManagerByToken(String token) {
        return getByToken(token, "admin", Manager.class);
    }

    public User getUserByToken(String token) {
        return getByToken(token, "user", User.class);
    }
}
