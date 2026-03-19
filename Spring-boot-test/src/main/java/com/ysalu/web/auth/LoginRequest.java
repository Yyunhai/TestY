package com.ysalu.web.auth;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求参数，支持用户名或邮箱登录。
 */
public class LoginRequest {

    @NotBlank(message = "Username or email is required.")
    private String usernameOrEmail;

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
