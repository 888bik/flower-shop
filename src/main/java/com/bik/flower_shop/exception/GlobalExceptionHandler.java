package com.bik.flower_shop.exception;

import com.bik.flower_shop.common.ApiResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice(basePackages = "com.bik.flower_shop")
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ApiResult<?> handleInvalidToken(InvalidTokenException ex) {
        return ApiResult.fail(ex.getMessage(), 401);
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResult<?> handleRuntime(RuntimeException ex) {
        return ApiResult.fail(ex.getMessage(), 500);
    }
}
