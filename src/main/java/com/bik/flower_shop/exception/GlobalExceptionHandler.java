package com.bik.flower_shop.exception;

import com.bik.flower_shop.common.ApiResult;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;


/**
 * @author bik
 */
@RestControllerAdvice(basePackages = "com.bik.flower_shop")
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResult<?> handleBusinessException(BusinessException e) {
        return ApiResult.fail(e.getMessage(), e.getErrorCode());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ApiResult<?> handleInvalidToken(InvalidTokenException ex) {
        return ApiResult.fail(ex.getMessage(), 401);
    }


    // 捕获 JSON 请求体缺失或格式错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<?> handleJsonParseError(HttpMessageNotReadableException ex) {
        return ApiResult.fail("请求体错误或缺失: " + ex.getMostSpecificCause().getMessage(), 40001);
    }

    // 捕获参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> handleValidationException(MethodArgumentNotValidException ex) {
        String message = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        return ApiResult.fail("参数校验失败: " + message, 40002);
    }


    // 捕获所有运行时异常
    @ExceptionHandler(RuntimeException.class)
    public ApiResult<?> handleRuntimeException(RuntimeException ex) {
        return ApiResult.fail(ex.getMessage(), 50000);
    }

    // 捕获所有其他异常
    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleException(Exception ex) {
        return ApiResult.fail("服务器错误" + ex.getMessage(), 50001);
    }


}
