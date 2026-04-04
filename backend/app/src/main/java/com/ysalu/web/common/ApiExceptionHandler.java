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

// 统一接口异常处理器。
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // 处理业务认证异常。
    public AuthResponse handleAuthException(AuthException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    // 处理未登录异常。
    public AuthResponse handleUnauthorizedException(UnauthorizedException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    // 处理无权限异常。
    public AuthResponse handleForbiddenException(ForbiddenException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // 优先返回字段校验的首个错误信息。
    public AuthResponse handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "Request is invalid." : fieldError.getDefaultMessage();
        return new AuthResponse(false, message, null, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // 处理约束校验异常。
    public AuthResponse handleConstraintViolationException(ConstraintViolationException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    @ExceptionHandler(DocumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // 处理文档模块抛出的业务异常。
    public AuthResponse handleDocumentException(DocumentException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }
}
