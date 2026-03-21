package com.ysalu.web.auth;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 重置密码请求参数。
 * 通过用户名和邮箱共同确认账号归属，避免仅凭用户名重置密码。
 */
public class ResetPasswordRequest {

    /**
     * 用户名。
     */
    @NotBlank(message = "Username is required.")
    @Size(min = 6, message = "Username must be at least 6 characters.")
    private String username;

    /**
     * 邮箱。
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    private String email;

    /**
     * 新密码。
     */
    @NotBlank(message = "New password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters.")
    private String newPassword;

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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
