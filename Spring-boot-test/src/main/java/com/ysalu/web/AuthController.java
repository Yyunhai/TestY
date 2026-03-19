package com.ysalu.web;

import com.ysalu.domain.UserAccount;
import com.ysalu.service.AuthService;
import com.ysalu.web.auth.AuthResponse;
import com.ysalu.web.auth.LoginRequest;
import com.ysalu.web.auth.RegisterRequest;
import com.ysalu.web.auth.ResetPasswordRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
/**
 * 暴露注册、登录和找回密码接口。
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    /**
     * 处理用户注册请求。
     */
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        // 控制层保持轻量，核心注册规则集中在服务层处理。
        UserAccount userAccount = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );
        return new AuthResponse(
                true,
                "Registration successful.",
                userAccount.getUsername(),
                userAccount.getEmail()
        );
    }

    @PostMapping("/login")
    /**
     * 处理用户登录请求。
     */
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        // 这里只返回前端展示所需的最小用户信息，避免暴露敏感字段。
        UserAccount userAccount = authService.login(
                request.getUsernameOrEmail(),
                request.getPassword()
        );
        return new AuthResponse(
                true,
                "Login successful.",
                userAccount.getUsername(),
                userAccount.getEmail()
        );
    }

    @PostMapping("/reset-password")
    /**
     * 处理忘记密码后的密码重置请求。
     */
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // 重置密码接口沿用统一响应结构，前端可以复用同一套消息提示逻辑。
        UserAccount userAccount = authService.resetPassword(
                request.getUsername(),
                request.getEmail(),
                request.getNewPassword()
        );
        return new AuthResponse(
                true,
                "Password reset successful.",
                userAccount.getUsername(),
                userAccount.getEmail()
        );
    }
}
