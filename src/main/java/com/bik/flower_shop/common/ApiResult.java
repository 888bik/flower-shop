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

    public static <T> ApiResult<T> fail(String msg, int code) {
        ApiResult<T> result = new ApiResult<>();
        result.setMsg(msg);
        result.setErrorCode(code);
        result.setData(null);
        return result;
    }


    public static ApiResult<?> ok() {
        return ok(null);
    }
}
