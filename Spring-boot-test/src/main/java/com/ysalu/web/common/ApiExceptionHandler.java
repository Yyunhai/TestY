package com.ysalu.web.common;

import com.ysalu.document.DocumentException;
import com.ysalu.service.common.AuthException;
import com.ysalu.web.auth.AuthResponse;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * 将服务层和控制器层抛出的异常统一包装为前端可直接消费的 JSON 响应。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 处理认证流程中的业务异常。
     */
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleAuthException(AuthException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    /**
     * 处理未登录异常。
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AuthResponse handleUnauthorizedException(UnauthorizedException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    /**
     * 处理越权异常。
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public AuthResponse handleForbiddenException(ForbiddenException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    /**
     * 处理请求体字段校验失败。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "Request is invalid." : fieldError.getDefaultMessage();
        return new AuthResponse(false, message, null, null);
    }

    /**
     * 处理参数级约束校验失败。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleConstraintViolationException(ConstraintViolationException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    /**
     * 处理文档模块业务异常。
     */
    @ExceptionHandler(DocumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleDocumentException(DocumentException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }
}
