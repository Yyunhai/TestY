package com.ysalu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysalu.web.auth.AuthResponse;
import com.ysalu.web.auth.SessionUser;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 基于 HttpSession 的请求拦截器。
 * 进入控制器之前先检查会话中是否存在已认证用户，不存在则直接返回 401 JSON。
 */
@Component
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public SessionAuthInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 预处理 API 请求。
     * `OPTIONS` 预检请求直接放行，其余请求必须带有合法的会话用户。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Object sessionUser = session == null ? null : session.getAttribute(SessionUser.SESSION_USER_ATTRIBUTE);
        if (sessionUser instanceof SessionUser) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new AuthResponse(false, "Authentication required.", null, null));
        return false;
    }
}
