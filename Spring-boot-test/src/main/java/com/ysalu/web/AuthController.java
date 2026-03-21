package com.ysalu.web;

import com.ysalu.service.AuthService;
import com.ysalu.service.AuthenticatedUser;
import com.ysalu.web.auth.AuthResponse;
import com.ysalu.web.auth.LoginRequest;
import com.ysalu.web.auth.RegisterRequest;
import com.ysalu.web.auth.ResetPasswordRequest;
import com.ysalu.web.auth.SessionUser;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器。
 * 提供注册、登录、查询当前会话、重置密码和退出登录等接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionAuthorization sessionAuthorization;

    public AuthController(AuthService authService, SessionAuthorization sessionAuthorization) {
        this.authService = authService;
        this.sessionAuthorization = sessionAuthorization;
    }

    /**
     * 注册新用户，同时写入账户表、资料表和默认角色关系。
     */
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        AuthenticatedUser authenticatedUser = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getDisplayName(),
                request.getPhoneNumber()
        );
        return buildResponse(true, "Registration successful.", authenticatedUser);
    }

    /**
     * 用户登录并建立 Session，会同时记录登录 IP、时间、角色与权限快照。
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        AuthenticatedUser authenticatedUser = authService.login(
                request.getUsernameOrEmail(),
                request.getPassword(),
                resolveClientIp(httpServletRequest),
                httpServletRequest.getHeader("User-Agent")
        );
        createSession(httpServletRequest, authenticatedUser);
        return buildResponse(true, "Login successful.", authenticatedUser);
    }

    /**
     * 返回当前会话对应的最新用户信息。
     */
    @GetMapping("/me")
    public AuthResponse currentUser(HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requireAuthenticatedUser(session);
        AuthenticatedUser authenticatedUser = authService.getAuthenticatedUser(sessionUser.getId());
        return buildResponse(true, "Authenticated.", authenticatedUser);
    }

    /**
     * 按用户名和邮箱匹配后重置密码。
     */
    @PostMapping("/reset-password")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        AuthenticatedUser authenticatedUser = authService.resetPassword(
                request.getUsername(),
                request.getEmail(),
                request.getNewPassword()
        );
        return buildResponse(true, "Password reset successful.", authenticatedUser);
    }

    /**
     * 注销当前会话。
     */
    @PostMapping("/logout")
    public AuthResponse logout(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new AuthResponse(true, "Logout successful.", null, null);
    }

    /**
     * 重新建立会话，避免旧会话残留。
     */
    private void createSession(HttpServletRequest httpServletRequest, AuthenticatedUser authenticatedUser) {
        HttpSession existingSession = httpServletRequest.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(SessionUser.SESSION_USER_ATTRIBUTE, SessionUser.from(authenticatedUser));
    }

    /**
     * 将服务层用户对象转换成统一的认证响应。
     */
    private AuthResponse buildResponse(boolean success, String message, AuthenticatedUser authenticatedUser) {
        return new AuthResponse(
                success,
                message,
                authenticatedUser.getUsername(),
                authenticatedUser.getEmail(),
                authenticatedUser.getDisplayName(),
                authenticatedUser.getPhoneNumber(),
                authenticatedUser.getAccountStatus(),
                authenticatedUser.getRoles(),
                authenticatedUser.getPermissions(),
                authenticatedUser.isRootAdmin(),
                authenticatedUser.getLastLoginAt(),
                authenticatedUser.getLastLoginIp()
        );
    }

    /**
     * 优先从反向代理请求头中解析真实客户端 IP，未命中时退回到直接连接地址。
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            String[] parts = forwardedFor.split(",");
            if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                return parts[0].trim();
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
