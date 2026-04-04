package com.ysalu.service.common;

// 认证异常。

public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
