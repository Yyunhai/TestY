package com.ysalu.service;

/**
 * 认证流程中的业务异常，统一交给控制层异常处理器转换为接口响应。
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
