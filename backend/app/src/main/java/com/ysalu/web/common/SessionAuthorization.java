package com.ysalu.web.common;

import com.ysalu.web.auth.SessionUser;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

// 会话鉴权辅助组件。
@Component
public class SessionAuthorization {

    // 要求请求上下文中已经存在登录用户。
    public SessionUser requireAuthenticatedUser(HttpSession session) {
        Object sessionUser = session == null ? null : session.getAttribute(SessionUser.SESSION_USER_ATTRIBUTE);
        if (sessionUser instanceof SessionUser) {
            return (SessionUser) sessionUser;
        }
        throw new UnauthorizedException("Authentication required.");
    }

    // 在登录基础上继续校验指定权限。
    public SessionUser requirePermission(HttpSession session, String permission) {
        SessionUser sessionUser = requireAuthenticatedUser(session);
        if (sessionUser.isRootAdmin() || sessionUser.getPermissions().contains(permission)) {
            return sessionUser;
        }
        throw new ForbiddenException("Permission denied: " + permission);
    }
}
