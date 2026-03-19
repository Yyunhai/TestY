package com.ysalu.web.auth;

/**
 * 认证接口统一响应对象。
 */
public class AuthResponse {
    private final boolean success;
    private final String message;
    private final String username;
    private final String email;

    public AuthResponse(boolean success, String message, String username, String email) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.email = email;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
