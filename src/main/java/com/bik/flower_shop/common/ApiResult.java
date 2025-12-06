package com.bik.flower_shop.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应封装
 *
 * @author bik
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    private String msg;
    private Integer errorCode;
    private T data;

    /**
     * 成功返回，带数据
     */
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>("success", 0, data);
    }

    /**
     * 成功返回，无数据
     */
    public static <T> ApiResult<T> ok() {
        return ok(null);
    }

    /**
     * 失败返回，自定义错误码
     */
    public static <T> ApiResult<T> fail(String msg, int errorCode) {
        return new ApiResult<>(msg, errorCode, null);
    }

    /**
     * 失败返回，默认错误码 -1
     */
    public static <T> ApiResult<T> fail(String msg) {
        return fail(msg, -1);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return errorCode != null && errorCode == 0;
    }
}
