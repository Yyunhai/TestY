package com.ysalu.web.auth;

import com.ysalu.service.auth.AuthService;
import com.ysalu.service.auth.AuthenticatedUser;
import com.ysalu.service.log.OperationLogService;
import com.ysalu.web.common.SessionAuthorization;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 认证接口控制器。
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionAuthorization sessionAuthorization;
    private final OperationLogService operationLogService;

    public AuthController(
            AuthService authService,
            SessionAuthorization sessionAuthorization,
            OperationLogService operationLogService
    ) {
        this.authService = authService;
        this.sessionAuthorization = sessionAuthorization;
        this.operationLogService = operationLogService;
    }

    // 注册新用户并返回用户快照。
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpServletRequest) {
        AuthenticatedUser authenticatedUser = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getDisplayName(),
                request.getPhoneNumber(),
                resolveClientIp(httpServletRequest)
        );
        return buildResponse(true, "Registration successful.", authenticatedUser);
    }

    // 校验凭证并建立新的登录会话。
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

    // 读取当前会话对应的最新用户信息。
    @GetMapping("/me")
    public AuthResponse currentUser(HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requireAuthenticatedUser(session);
        AuthenticatedUser authenticatedUser = authService.getAuthenticatedUser(sessionUser.getId());
        return buildResponse(true, "Authenticated.", authenticatedUser);
    }

    // 按用户名和邮箱重置密码。
    @PostMapping("/reset-password")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpServletRequest) {
        AuthenticatedUser authenticatedUser = authService.resetPassword(
                request.getUsername(),
                request.getEmail(),
                request.getNewPassword(),
                resolveClientIp(httpServletRequest)
        );
        return buildResponse(true, "Password reset successful.", authenticatedUser);
    }

    // 注销当前会话，并记录退出日志。
    @PostMapping("/logout")
    public AuthResponse logout(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            Object sessionUserObject = session.getAttribute(SessionUser.SESSION_USER_ATTRIBUTE);
            if (sessionUserObject instanceof SessionUser) {
                SessionUser sessionUser = (SessionUser) sessionUserObject;
                operationLogService.record(
                        sessionUser.getId(),
                        sessionUser.getUsername(),
                        "AUTH",
                        "USER_LOGOUT",
                        "SESSION",
                        session.getId(),
                        true,
                        "User logged out.",
                        null,
                        resolveClientIp(httpServletRequest)
                );
            }
            session.invalidate();
        }
        return new AuthResponse(true, "Logout successful.", null, null);
    }

    // 登录成功后重建会话，避免旧 Session 继续复用。
    private void createSession(HttpServletRequest httpServletRequest, AuthenticatedUser authenticatedUser) {
        HttpSession existingSession = httpServletRequest.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(SessionUser.SESSION_USER_ATTRIBUTE, SessionUser.from(authenticatedUser));
    }

    // 把服务层对象转换成统一的接口响应结构。
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

    // 优先读取反向代理头中的真实客户端 IP。
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
