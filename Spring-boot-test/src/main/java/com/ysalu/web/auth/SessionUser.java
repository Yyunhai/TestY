package com.ysalu.web.auth;

import com.ysalu.domain.UserAccount;
import java.io.Serializable;

/**
 * Minimal authenticated user snapshot stored in HttpSession.
 */
public class SessionUser implements Serializable {

    public static final String SESSION_USER_ATTRIBUTE = "sessionUser";

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String email;

    public SessionUser(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public static SessionUser from(UserAccount userAccount) {
        return new SessionUser(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getEmail()
        );
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
