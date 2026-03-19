package com.ysalu.service;

import com.ysalu.domain.UserAccount;
import com.ysalu.repository.UserAccountRepository;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
/**
 * 负责注册、登录和找回密码等认证业务。
 */
public class AuthService {

    // 这里只做基础邮箱格式校验，核心目标是统一输入并拦截明显非法值。
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 注册新用户，并在入库前完成用户名、邮箱和密码校验。
     */
    public UserAccount register(String username, String email, String rawPassword) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        validatePassword(rawPassword);

        // 先校验用户名唯一性，避免创建重复账号。
        if (userAccountRepository.existsByUsername(normalizedUsername)) {
            throw new AuthException("Username already exists.");
        }

        // 邮箱同样作为唯一标识之一，重复时直接中断注册。
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new AuthException("Email already exists.");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(normalizedUsername);
        userAccount.setEmail(normalizedEmail);
        // 密码只保存编码后的摘要，不直接落库存储明文。
        userAccount.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userAccountRepository.save(userAccount);
    }

    /**
     * 支持用户名或邮箱登录，并校验密码是否匹配。
     */
    public UserAccount login(String usernameOrEmail, String rawPassword) {
        String principal = usernameOrEmail == null ? "" : usernameOrEmail.trim();
        if (principal.isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new AuthException("Username/email and password are required.");
        }

        // 输入里包含 @ 时按邮箱登录，否则按用户名登录。
        UserAccount userAccount = principal.contains("@")
                ? userAccountRepository.findByEmail(normalizeEmail(principal))
                .orElseThrow(() -> new AuthException("Account does not exist."))
                : userAccountRepository.findByUsername(principal)
                .orElseThrow(() -> new AuthException("Account does not exist."));

        // 统一交给密码编码器校验摘要是否匹配。
        if (!passwordEncoder.matches(rawPassword, userAccount.getPasswordHash())) {
            throw new AuthException("Incorrect password.");
        }

        return userAccount;
    }

    /**
     * 通过用户名和邮箱找回账号，并重置为新密码。
     */
    public UserAccount resetPassword(String username, String email, String newPassword) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        validatePassword(newPassword);

        // 先通过邮箱锁定账号，再核对用户名，降低误重置其他账号的风险。
        UserAccount userAccount = userAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthException("Account does not exist."));

        if (!userAccount.getUsername().equals(normalizedUsername)) {
            throw new AuthException("Username and email do not match.");
        }

        // 新密码沿用相同的加密策略保存。
        userAccount.setPasswordHash(passwordEncoder.encode(newPassword));
        return userAccountRepository.save(userAccount);
    }

    /**
     * 统一清洗用户名，并保证长度符合注册规则。
     */
    private String normalizeUsername(String username) {
        String value = username == null ? "" : username.trim();
        if (value.length() < 6) {
            throw new AuthException("Username must be at least 6 characters.");
        }
        return value;
    }

    /**
     * 统一清洗邮箱，避免大小写差异导致重复账号。
     */
    private String normalizeEmail(String email) {
        String value = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new AuthException("Email format is invalid.");
        }
        return value;
    }

    /**
     * 当前密码规则为不少于 6 位。
     */
    private void validatePassword(String rawPassword) {
        String value = rawPassword == null ? "" : rawPassword.trim();
        if (value.length() < 6) {
            throw new AuthException("Password must be at least 6 characters.");
        }
    }
}
