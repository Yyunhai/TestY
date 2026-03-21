package com.ysalu.web;

/**
 * 未登录异常。
 * 当受保护接口在没有有效会话的情况下被访问时抛出。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
