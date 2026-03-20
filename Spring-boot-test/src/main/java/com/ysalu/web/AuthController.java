package com.ysalu.web;

import com.ysalu.domain.UserAccount;
import com.ysalu.service.AuthService;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
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
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        UserAccount userAccount = authService.login(
                request.getUsernameOrEmail(),
                request.getPassword()
        );
        createSession(httpServletRequest, userAccount);
        return new AuthResponse(
                true,
                "Login successful.",
                userAccount.getUsername(),
                userAccount.getEmail()
        );
    }

    @GetMapping("/me")
    public AuthResponse currentUser(HttpSession session) {
        SessionUser sessionUser = requireSessionUser(session);
        return new AuthResponse(
                true,
                "Authenticated.",
                sessionUser.getUsername(),
                sessionUser.getEmail()
        );
    }

    @PostMapping("/reset-password")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
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

    @PostMapping("/logout")
    public AuthResponse logout(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new AuthResponse(true, "Logout successful.", null, null);
    }

    private void createSession(HttpServletRequest httpServletRequest, UserAccount userAccount) {
        HttpSession existingSession = httpServletRequest.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(SessionUser.SESSION_USER_ATTRIBUTE, SessionUser.from(userAccount));
    }

    private SessionUser requireSessionUser(HttpSession session) {
        Object sessionUser = session == null ? null : session.getAttribute(SessionUser.SESSION_USER_ATTRIBUTE);
        if (sessionUser instanceof SessionUser) {
            return (SessionUser) sessionUser;
        }
        throw new UnauthorizedException("Authentication required.");
    }
}
