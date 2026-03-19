package com.ysalu.web;

import com.ysalu.service.AuthException;
import com.ysalu.web.auth.AuthResponse;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * 统一处理接口层抛出的业务异常和参数校验异常。
 */
public class ApiExceptionHandler {

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * 处理认证业务异常，返回统一的失败响应结构。
     */
    public AuthResponse handleAuthException(AuthException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * 处理请求体字段校验失败的情况。
     */
    public AuthResponse handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        // 优先返回首个字段错误，让前端能先提示用户修复最关键的问题。
        String message = fieldError == null ? "Request is invalid." : fieldError.getDefaultMessage();
        return new AuthResponse(false, message, null, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * 处理简单参数校验失败的情况。
     */
    public AuthResponse handleConstraintViolationException(ConstraintViolationException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }
}
