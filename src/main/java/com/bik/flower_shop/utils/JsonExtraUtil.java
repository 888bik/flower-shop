package com.bik.flower_shop.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON extra 字段工具类
 * 主要用于 Orders.extra / ship_data 等 JSON 字段
 * @author bik
 */
@Component
public class JsonExtraUtil {

    private final ObjectMapper objectMapper;

    public JsonExtraUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 解析 extra JSON 字符串为 Map
     */
    public Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 对象转 JSON 字符串
     */
    public String toJson(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 向 extra 中写入一个字段
     */
    public String put(String json, String key, Object value) {
        Map<String, Object> map = parse(json);
        map.put(key, value);
        return toJson(map);
    }
}
