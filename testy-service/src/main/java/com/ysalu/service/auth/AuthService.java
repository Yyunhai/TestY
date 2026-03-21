package com.ysalu.service.auth;

import com.ysalu.domain.auth.AccountStatus;
import com.ysalu.domain.auth.LoginAudit;
import com.ysalu.domain.auth.RolePermission;
import com.ysalu.domain.auth.SystemRole;
import com.ysalu.domain.auth.UserAccount;
import com.ysalu.domain.auth.UserProfile;
import com.ysalu.domain.auth.UserRole;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证核心服务。
 * 负责注册、登录校验、重置密码、角色权限组装和登录审计记录。
 */
@Service
public class AuthService {

    /**
     * 邮箱格式校验表达式。
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final SystemRoleRepository systemRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final LoginAuditRepository loginAuditRepository;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            UserRoleRepository userRoleRepository,
            SystemRoleRepository systemRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            LoginAuditRepository loginAuditRepository,
            PasswordEncoder passwordEncoder,
            OperationLogService operationLogService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRoleRepository = userRoleRepository;
        this.systemRoleRepository = systemRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.loginAuditRepository = loginAuditRepository;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
    }

    /**
     * 注册新用户。
     * 会写入账户表、资料表，并分配默认 USER 角色。
     */
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

    /**
     * 登录并记录审计信息。
     * 登录成功后会回写最近登录时间和 IP，并持久化角色、权限快照。
     */
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

        if (!passwordEncoder.matches(rawPassword, userAccount.getPasswordHash())) {
            recordLoginAttempt(userAccount, principal, remoteIp, userAgent, false, "Incorrect password.",
                    Collections.<String>emptyList(), Collections.<String>emptyList());
            throw new AuthException("Incorrect password.");
        }

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

    /**
     * 根据用户名和邮箱校验后重置密码。
     */
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

    /**
     * 根据用户 ID 重新加载最新认证信息。
     */
    @Transactional(readOnly = true)
    public AuthenticatedUser getAuthenticatedUser(Long userId) {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Account does not exist."));
        return loadAuthenticatedUser(userAccount);
    }

    /**
     * 校验账户状态是否允许登录和操作。
     */
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

    /**
     * 记录一次登录尝试，无论成功还是失败都会保留审计痕迹。
     */
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
    }

    /**
     * 将角色或权限快照序列化为逗号分隔字符串，便于审计表保存。
     */
    private String joinSnapshot(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream().collect(Collectors.joining(","));
    }

    /**
     * 为用户补充分配角色，避免重复关系。
     */
    private void assignRoleIfAbsent(UserAccount userAccount, SystemRole role) {
        if (userRoleRepository.existsByUserAccount_IdAndRole_Code(userAccount.getId(), role.getCode())) {
            return;
        }
        UserRole userRole = new UserRole();
        userRole.setUserAccount(userAccount);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }

    /**
     * 从账户、资料、角色和权限多张表中组装认证用户对象。
     */
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

    /**
     * 规范化用户名并做最小长度校验。
     */
    private String normalizeUsername(String username) {
        String value = username == null ? "" : username.trim();
        if (value.length() < 6) {
            throw new AuthException("Username must be at least 6 characters.");
        }
        return value;
    }

    /**
     * 规范化邮箱并转换为小写。
     */
    private String normalizeEmail(String email) {
        String value = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new AuthException("Email format is invalid.");
        }
        return value;
    }

    /**
     * 规范化显示名称，未填写时回退为用户名。
     */
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

    /**
     * 规范化手机号，允许为空。
     */
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

    /**
     * 校验密码长度。
     */
    private void validatePassword(String rawPassword) {
        String value = rawPassword == null ? "" : rawPassword.trim();
        if (value.length() < 6) {
            throw new AuthException("Password must be at least 6 characters.");
        }
    }

    /**
     * 截断超长字符串，防止审计字段溢出。
     */
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
