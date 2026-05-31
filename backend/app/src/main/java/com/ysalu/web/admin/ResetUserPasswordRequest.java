package com.ysalu.web.admin;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

// 管理员重置用户密码请求对象。

public class ResetUserPasswordRequest {

    @NotBlank(message = "New password is required.")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters.")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
