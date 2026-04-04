package com.ysalu.web.auth;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

// 重置Password请求对象。

public class ResetPasswordRequest {

    @NotBlank(message = "Username is required.")
    @Size(min = 6, message = "Username must be at least 6 characters.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    private String email;

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
