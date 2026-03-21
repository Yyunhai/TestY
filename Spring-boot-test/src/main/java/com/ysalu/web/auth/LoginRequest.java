package com.ysalu.web.auth;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求参数。
 * 支持使用用户名或邮箱作为登录主体。
 */
public class LoginRequest {

    /**
     * 用户名或邮箱。
     */
    @NotBlank(message = "Username or email is required.")
    private String usernameOrEmail;

    /**
     * 明文密码，进入服务层后会与加密后的密码摘要比对。
     */
    @NotBlank(message = "Password is required.")
    private String password;

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
