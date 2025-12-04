package com.bik.flower_shop.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private String msg;
    private Integer errorCode;
    private T data;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>("ok", null, data);
    }

    public static ApiResult<?> fail(String msg, Integer errorCode) {
        return new ApiResult<>(msg, errorCode, null);
    }

    public static ApiResult<?> ok() {
        return ok(null);
    }
}
