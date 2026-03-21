package com.ysalu.web.auth;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 注册请求参数。
 * 包含账户信息和可选的资料信息，注册成功后会在多张表中落库。
 */
public class RegisterRequest {

    /**
     * 用户名，最少 6 个字符。
     */
    @NotBlank(message = "Username is required.")
    @Size(min = 6, message = "Username must be at least 6 characters.")
    private String username;

    /**
     * 登录邮箱。
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    private String email;

    /**
     * 登录密码。
     */
    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters.")
    private String password;

    /**
     * 展示名称，可选。
     */
    private String displayName;

    /**
     * 手机号，可选。
     */
    private String phoneNumber;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
