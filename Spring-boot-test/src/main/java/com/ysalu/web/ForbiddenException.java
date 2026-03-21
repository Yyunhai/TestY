package com.ysalu.web;

/**
 * 越权异常。
 * 当已登录用户缺少目标接口所需权限时抛出。
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
