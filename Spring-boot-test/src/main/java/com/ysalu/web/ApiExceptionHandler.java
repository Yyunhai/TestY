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
public class ApiExceptionHandler {

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleAuthException(AuthException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AuthResponse handleUnauthorizedException(UnauthorizedException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "Request is invalid." : fieldError.getDefaultMessage();
        return new AuthResponse(false, message, null, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AuthResponse handleConstraintViolationException(ConstraintViolationException exception) {
        return new AuthResponse(false, exception.getMessage(), null, null);
    }
}
