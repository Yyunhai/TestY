package com.ysalu.web.common;

import com.ysalu.web.auth.SessionUser;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * 会话级权限检查组件。
 * 控制器通过它统一完成“是否已登录”和“是否具备某个权限”的校验。
 */
@Component
public class SessionAuthorization {

    /**
     * 要求当前请求必须带有合法登录会话。
     */
    public SessionUser requireAuthenticatedUser(HttpSession session) {
        Object sessionUser = session == null ? null : session.getAttribute(SessionUser.SESSION_USER_ATTRIBUTE);
        if (sessionUser instanceof SessionUser) {
            return (SessionUser) sessionUser;
        }
        throw new UnauthorizedException("Authentication required.");
    }

    /**
     * 要求当前用户具备指定权限。
     * root 管理员默认视为拥有全部权限。
     */
    public SessionUser requirePermission(HttpSession session, String permission) {
        SessionUser sessionUser = requireAuthenticatedUser(session);
        if (sessionUser.isRootAdmin() || sessionUser.getPermissions().contains(permission)) {
            return sessionUser;
        }
        throw new ForbiddenException("Permission denied: " + permission);
    }
}
