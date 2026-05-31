package com.ysalu.service.auth;

import com.ysalu.domain.auth.AccountStatus;
import com.ysalu.domain.auth.LoginAudit;
import com.ysalu.domain.auth.RolePermission;
import com.ysalu.domain.auth.SystemRole;
import com.ysalu.domain.auth.UserAccount;
import com.ysalu.domain.auth.UserProfile;
import com.ysalu.domain.auth.UserRole;
import com.ysalu.logging.ApplicationLogService;
import com.ysalu.repository.auth.LoginAuditRepository;
import com.ysalu.repository.auth.RolePermissionRepository;
import com.ysalu.repository.auth.SystemRoleRepository;
import com.ysalu.repository.auth.UserAccountRepository;
import com.ysalu.repository.auth.UserProfileRepository;
import com.ysalu.repository.auth.UserRoleRepository;
import com.ysalu.service.common.AuthException;
import com.ysalu.service.log.OperationLogService;
import com.ysalu.service.security.RoleCodes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 认证核心服务。
@Service
public class AuthService {

    // 基础邮箱格式校验规则。
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Value("${testy.security.login.max-attempts:5}")
    private int maxLoginAttempts;

    @Value("${testy.security.login.lock-minutes:30}")
    private int lockMinutes;

    @Value("${testy.security.password.min-length:8}")
    private int minPasswordLength;

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final SystemRoleRepository systemRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final LoginAuditRepository loginAuditRepository;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;
    private final ApplicationLogService applicationLogService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            UserRoleRepository userRoleRepository,
            SystemRoleRepository systemRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            LoginAuditRepository loginAuditRepository,
            PasswordEncoder passwordEncoder,
            OperationLogService operationLogService,
            ApplicationLogService applicationLogService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRoleRepository = userRoleRepository;
        this.systemRoleRepository = systemRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.loginAuditRepository = loginAuditRepository;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
        this.applicationLogService = applicationLogService;
    }

    // 注册用户、补齐资料并分配默认角色。
    @Transactional
    public AuthenticatedUser register(
            String username,
            String email,
            String rawPassword,
            String displayName,
            String phoneNumber,
            String remoteIp
    ) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        validatePassword(rawPassword);

        if ("root".equalsIgnoreCase(normalizedUsername)) {
            throw new AuthException("该用户名不允许注册。");
        }

        if (userAccountRepository.existsByUsername(normalizedUsername)) {
            throw new AuthException("Username already exists.");
        }
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new AuthException("Email already exists.");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(normalizedUsername);
        userAccount.setEmail(normalizedEmail);
        userAccount.setPasswordHash(passwordEncoder.encode(rawPassword));
        userAccount.setAccountStatus(AccountStatus.ACTIVE);
        UserAccount savedAccount = userAccountRepository.save(userAccount);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserAccount(savedAccount);
        userProfile.setDisplayName(normalizeDisplayName(displayName, normalizedUsername));
        userProfile.setPhoneNumber(normalizePhoneNumber(phoneNumber));
        userProfileRepository.save(userProfile);

        SystemRole defaultRole = systemRoleRepository.findByCode(RoleCodes.USER)
                .orElseThrow(() -> new AuthException("Default user role is missing."));
        assignRoleIfAbsent(savedAccount, defaultRole);

        AuthenticatedUser authenticatedUser = loadAuthenticatedUser(savedAccount);
        operationLogService.record(
                savedAccount.getId(),
                normalizedUsername,
                "AUTH",
                "USER_REGISTERED",
                "USER",
                savedAccount.getId(),
                true,
                "Registered a new user account.",
                "email=" + normalizedEmail,
                remoteIp
        );
        return authenticatedUser;
    }

    // 校验登录凭证，并写入登录审计记录。
    @Transactional(noRollbackFor = AuthException.class)
    public AuthenticatedUser login(String usernameOrEmail, String rawPassword, String remoteIp, String userAgent) {
        String principal = usernameOrEmail == null ? "" : usernameOrEmail.trim();
        if (principal.isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            recordLoginAttempt(null, principal, remoteIp, userAgent, false, "Username/email and password are required.",
                    Collections.<String>emptyList(), Collections.<String>emptyList());
            throw new AuthException("Username/email and password are required.");
        }

        UserAccount userAccount = principal.contains("@")
                ? userAccountRepository.findByEmail(normalizeEmail(principal)).orElse(null)
                : userAccountRepository.findByUsername(principal).orElse(null);

        if (userAccount == null) {
            recordLoginAttempt(null, principal, remoteIp, userAgent, false, "Account does not exist.",
                    Collections.<String>emptyList(), Collections.<String>emptyList());
            throw new AuthException("Account does not exist.");
        }

        try {
            ensureAccountIsActive(userAccount);
        } catch (AuthException exception) {
            recordLoginAttempt(userAccount, principal, remoteIp, userAgent, false, exception.getMessage(),
                    Collections.<String>emptyList(), Collections.<String>emptyList());
            throw exception;
        }

        if (isAccountLocked(userAccount)) {
            recordLoginAttempt(userAccount, principal, remoteIp, userAgent, false, "Account is temporarily locked.",
                    Collections.<String>emptyList(), Collections.<String>emptyList());
            throw new AuthException("Account is temporarily locked due to too many failed login attempts. Please try again later.");
        }

        if (!passwordEncoder.matches(rawPassword, userAccount.getPasswordHash())) {
            handleLoginFailure(userAccount);
            recordLoginAttempt(userAccount, principal, remoteIp, userAgent, false, "Incorrect password.",
                    Collections.<String>emptyList(), Collections.<String>emptyList());
            throw new AuthException("Incorrect password.");
        }

        userAccount.setFailedLoginAttempts(0);
        userAccount.setLockedUntil(null);
        AuthenticatedUser authenticatedUser = loadAuthenticatedUser(userAccount);
        LocalDateTime now = LocalDateTime.now();
        userAccount.setLastLoginAt(now);
        userAccount.setLastLoginIp(remoteIp);
        userAccountRepository.save(userAccount);
        authenticatedUser.setLastLoginAt(now);
        authenticatedUser.setLastLoginIp(remoteIp);
        recordLoginAttempt(
                userAccount,
                principal,
                remoteIp,
                userAgent,
                true,
                "Login successful.",
                authenticatedUser.getRoles(),
                authenticatedUser.getPermissions()
        );
        return authenticatedUser;
    }

    // 按用户名和邮箱重置密码。
    @Transactional
    public AuthenticatedUser resetPassword(String username, String email, String newPassword, String remoteIp) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        validatePassword(newPassword);

        UserAccount userAccount = userAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthException("Account does not exist."));
        if (!userAccount.getUsername().equals(normalizedUsername)) {
            throw new AuthException("Username and email do not match.");
        }

        ensureAccountIsActive(userAccount);
        userAccount.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(userAccount);

        AuthenticatedUser authenticatedUser = loadAuthenticatedUser(userAccount);
        operationLogService.record(
                userAccount.getId(),
                userAccount.getUsername(),
                "AUTH",
                "PASSWORD_RESET",
                "USER",
                userAccount.getId(),
                true,
                "Reset account password.",
                "email=" + normalizedEmail,
                remoteIp
        );
        return authenticatedUser;
    }

    // 根据用户 ID 重新装配最新认证信息。
    @Transactional(readOnly = true)
    public AuthenticatedUser getAuthenticatedUser(Long userId) {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Account does not exist."));
        return loadAuthenticatedUser(userAccount);
    }

    // 拒绝被禁用、锁定或状态异常的账户继续登录。
    private boolean isAccountLocked(UserAccount userAccount) {
        return userAccount.getLockedUntil() != null && LocalDateTime.now().isBefore(userAccount.getLockedUntil());
    }

    private void handleLoginFailure(UserAccount userAccount) {
        int attempts = userAccount.getFailedLoginAttempts() + 1;
        userAccount.setFailedLoginAttempts(attempts);
        if (attempts >= maxLoginAttempts) {
            userAccount.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
        }
        userAccountRepository.save(userAccount);
    }

    // 拒绝被禁用、锁定或状态异常的账户继续登录。
    private void ensureAccountIsActive(UserAccount userAccount) {
        if (userAccount.getAccountStatus() == null || userAccount.getAccountStatus() == AccountStatus.UNKNOWN) {
            throw new AuthException("Account status is invalid.");
        }
        if (userAccount.getAccountStatus() == AccountStatus.LOCKED) {
            throw new AuthException("Account is locked.");
        }
        if (userAccount.getAccountStatus() == AccountStatus.DISABLED) {
            throw new AuthException("Account is disabled.");
        }
    }

    // 无论成功还是失败，都把登录尝试落到审计表和应用日志。
    private void recordLoginAttempt(
            UserAccount userAccount,
            String principal,
            String remoteIp,
            String userAgent,
            boolean success,
            String message,
            Collection<String> roles,
            Collection<String> permissions
    ) {
        LoginAudit loginAudit = new LoginAudit();
        loginAudit.setUserAccount(userAccount);
        loginAudit.setPrincipal(principal == null || principal.trim().isEmpty() ? "unknown" : principal.trim());
        loginAudit.setRemoteIp(remoteIp == null || remoteIp.trim().isEmpty() ? "unknown" : remoteIp.trim());
        loginAudit.setUserAgent(truncate(userAgent, 255));
        loginAudit.setSuccess(success);
        loginAudit.setMessage(message);
        loginAudit.setRolesSnapshot(joinSnapshot(roles));
        loginAudit.setPermissionsSnapshot(joinSnapshot(permissions));
        loginAuditRepository.save(loginAudit);
        applicationLogService.security(
                success,
                success ? "LOGIN_SUCCESS" : "LOGIN_FAILURE",
                message,
                "principal", loginAudit.getPrincipal(),
                "remoteIp", loginAudit.getRemoteIp(),
                "userId", userAccount == null ? null : userAccount.getId(),
                "userAgent", loginAudit.getUserAgent(),
                "roles", loginAudit.getRolesSnapshot(),
                "permissions", loginAudit.getPermissionsSnapshot()
        );
    }

    private String joinSnapshot(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream().collect(Collectors.joining(","));
    }

    // 仅在用户还没有该角色时新增关联。
    private void assignRoleIfAbsent(UserAccount userAccount, SystemRole role) {
        if (userRoleRepository.existsByUserAccount_IdAndRole_Code(userAccount.getId(), role.getCode())) {
            return;
        }
        UserRole userRole = new UserRole();
        userRole.setUserAccount(userAccount);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }

    // 聚合账户、资料、角色和权限，生成完整登录用户快照。
    private AuthenticatedUser loadAuthenticatedUser(UserAccount userAccount) {
        UserProfile userProfile = userProfileRepository.findByUserAccount_Id(userAccount.getId())
                .orElseThrow(() -> new AuthException("User profile is missing."));
        List<UserRole> userRoles = userRoleRepository.findAllByUserAccount_Id(userAccount.getId());
        List<String> roleCodes = new ArrayList<String>();
        List<Long> roleIds = new ArrayList<Long>();
        for (UserRole userRole : userRoles) {
            roleCodes.add(userRole.getRole().getCode());
            roleIds.add(userRole.getRole().getId());
        }

        List<RolePermission> rolePermissions = roleIds.isEmpty()
                ? Collections.<RolePermission>emptyList()
                : rolePermissionRepository.findAllByRole_IdIn(roleIds);

        Set<String> permissionCodes = new LinkedHashSet<String>();
        for (RolePermission rolePermission : rolePermissions) {
            permissionCodes.add(rolePermission.getPermission().getCode());
        }

        return new AuthenticatedUser(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getEmail(),
                userProfile.getDisplayName(),
                userProfile.getPhoneNumber(),
                userAccount.getAccountStatus() == null ? AccountStatus.UNKNOWN.name() : userAccount.getAccountStatus().name(),
                roleCodes,
                new ArrayList<String>(permissionCodes),
                roleCodes.contains(RoleCodes.ROOT),
                userAccount.getLastLoginAt(),
                userAccount.getLastLoginIp()
        );
    }

    private String normalizeUsername(String username) {
        String value = username == null ? "" : username.trim();
        if (value.length() < 6) {
            throw new AuthException("Username must be at least 6 characters.");
        }
        return value;
    }

    private String normalizeEmail(String email) {
        String value = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new AuthException("Email format is invalid.");
        }
        return value;
    }

    private String normalizeDisplayName(String displayName, String fallbackUsername) {
        String value = displayName == null ? "" : displayName.trim();
        if (value.isEmpty()) {
            return fallbackUsername;
        }
        if (value.length() > 100) {
            throw new AuthException("Display name must be at most 100 characters.");
        }
        return value;
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String value = phoneNumber == null ? "" : phoneNumber.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > 32) {
            throw new AuthException("Phone number must be at most 32 characters.");
        }
        return value;
    }

    private void validatePassword(String rawPassword) {
        String value = rawPassword == null ? "" : rawPassword.trim();
        if (value.length() < minPasswordLength) {
            throw new AuthException("Password must be at least " + minPasswordLength + " characters.");
        }
        boolean hasUpper = !value.equals(value.toLowerCase());
        boolean hasLower = !value.equals(value.toUpperCase());
        boolean hasDigit = value.matches(".*\\d.*");
        boolean hasSpecial = value.matches(".*[^A-Za-z0-9].*");
        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new AuthException("Password must contain uppercase letters, lowercase letters, digits, and special characters.");
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
