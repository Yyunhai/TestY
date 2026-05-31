package com.ysalu.service.admin;

import com.ysalu.domain.audit.OperationLog;
import com.ysalu.domain.auth.AccountStatus;
import com.ysalu.domain.auth.LoginAudit;
import com.ysalu.domain.auth.RolePermission;
import com.ysalu.domain.auth.SystemPermission;
import com.ysalu.domain.auth.SystemRole;
import com.ysalu.domain.auth.UserAccount;
import com.ysalu.domain.auth.UserProfile;
import com.ysalu.domain.auth.UserRole;
import com.ysalu.repository.audit.OperationLogRepository;
import com.ysalu.repository.auth.FailedLoginAlertProjection;
import com.ysalu.repository.auth.LoginAuditRepository;
import com.ysalu.repository.auth.RolePermissionRepository;
import com.ysalu.repository.auth.SystemPermissionRepository;
import com.ysalu.repository.auth.SystemRoleRepository;
import com.ysalu.repository.auth.UserAccountRepository;
import com.ysalu.repository.auth.UserProfileRepository;
import com.ysalu.repository.auth.UserRoleRepository;
import com.ysalu.service.common.AuthException;
import com.ysalu.service.log.OperationLogService;
import com.ysalu.service.security.RoleCodes;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 后台管理服务。
@Service
public class AdminService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final LoginAuditRepository loginAuditRepository;
    private final OperationLogRepository operationLogRepository;
    private final SystemRoleRepository systemRoleRepository;
    private final SystemPermissionRepository systemPermissionRepository;
    private final OperationLogService operationLogService;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public AdminService(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            LoginAuditRepository loginAuditRepository,
            OperationLogRepository operationLogRepository,
            SystemRoleRepository systemRoleRepository,
            SystemPermissionRepository systemPermissionRepository,
            OperationLogService operationLogService,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.loginAuditRepository = loginAuditRepository;
        this.operationLogRepository = operationLogRepository;
        this.systemRoleRepository = systemRoleRepository;
        this.systemPermissionRepository = systemPermissionRepository;
        this.operationLogService = operationLogService;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    // 组装用户列表，供后台用户管理页展示。
    public List<AdminUserView> listUsers() {
        List<UserAccount> accounts = userAccountRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
        List<AdminUserView> result = new ArrayList<AdminUserView>();
        for (UserAccount account : accounts) {
            result.add(buildAdminUserViewSafely(account));
        }
        return result;
    }

    @Transactional(readOnly = true)
    // 按筛选条件分页返回登录审计记录。
    public Page<LoginAuditView> listLoginAudits(int page, int size, String principal, Boolean success, String remoteIp) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "loggedInAt"));
        List<LoginAudit> audits = loginAuditRepository.findAll(Sort.by(Sort.Direction.DESC, "loggedInAt"));
        List<LoginAuditView> filtered = new ArrayList<LoginAuditView>();
        String normalizedPrincipal = normalizeFilter(principal);
        String normalizedRemoteIp = normalizeFilter(remoteIp);
        for (LoginAudit audit : audits) {
            if (!normalizedPrincipal.isEmpty()
                    && (audit.getPrincipal() == null
                    || !audit.getPrincipal().toLowerCase(Locale.ROOT).contains(normalizedPrincipal))) {
                continue;
            }
            if (success != null && audit.isSuccess() != success.booleanValue()) {
                continue;
            }
            if (!normalizedRemoteIp.isEmpty()
                    && (audit.getRemoteIp() == null
                    || !audit.getRemoteIp().toLowerCase(Locale.ROOT).contains(normalizedRemoteIp))) {
                continue;
            }
            filtered.add(toAuditView(audit));
        }
        return toPage(filtered, pageable);
    }

    @Transactional(readOnly = true)
    // 汇总最近 24 小时内的登录风险数据。
    public LoginAuditAlertView getLoginAuditAlerts() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        long failedAttempts = loginAuditRepository.countBySuccessFalseAndLoggedInAtAfter(since);
        List<FailedLoginAlertProjection> projections = loginAuditRepository.findFrequentFailedPrincipals(since, 3);
        List<FailedLoginAlertView> suspiciousPrincipals = new ArrayList<FailedLoginAlertView>();
        for (FailedLoginAlertProjection projection : projections) {
            suspiciousPrincipals.add(new FailedLoginAlertView(
                    projection.getPrincipal(),
                    projection.getFailureCount(),
                    projection.getLatestFailureAt(),
                    projection.getLatestRemoteIp()
            ));
        }
        return new LoginAuditAlertView(failedAttempts, suspiciousPrincipals);
    }

    @Transactional(readOnly = true)
    // 按模块、动作和结果分页查询操作日志。
    public Page<OperationLogView> listOperationLogs(
            int page,
            int size,
            String module,
            String action,
            String operatorUsername,
            Boolean success
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "occurredAt"));
        List<OperationLog> logs = operationLogRepository.findAll(Sort.by(Sort.Direction.DESC, "occurredAt"));
        List<OperationLogView> filtered = new ArrayList<OperationLogView>();
        String normalizedModule = normalizeFilter(module);
        String normalizedAction = normalizeFilter(action);
        String normalizedOperatorUsername = normalizeFilter(operatorUsername);
        for (OperationLog log : logs) {
            if (!normalizedModule.isEmpty()
                    && (log.getModuleCode() == null
                    || !log.getModuleCode().toLowerCase(Locale.ROOT).contains(normalizedModule))) {
                continue;
            }
            if (!normalizedAction.isEmpty()
                    && (log.getActionCode() == null
                    || !log.getActionCode().toLowerCase(Locale.ROOT).contains(normalizedAction))) {
                continue;
            }
            if (!normalizedOperatorUsername.isEmpty()
                    && (log.getOperatorUsername() == null
                    || !log.getOperatorUsername().toLowerCase(Locale.ROOT).contains(normalizedOperatorUsername))) {
                continue;
            }
            if (success != null && log.isSuccess() != success.booleanValue()) {
                continue;
            }
            filtered.add(toOperationLogView(log));
        }
        return toPage(filtered, pageable);
    }

    @Transactional(readOnly = true)
    // 查询角色及其权限集合。
    public List<RoleView> listRoles() {
        List<SystemRole> roles = systemRoleRepository.findAllByOrderByBuiltInDescCodeAsc();
        List<RoleView> result = new ArrayList<RoleView>();
        for (SystemRole role : roles) {
            result.add(toRoleView(role));
        }
        return result;
    }

    @Transactional(readOnly = true)
    // 查询全部系统权限。
    public List<PermissionView> listPermissions() {
        List<SystemPermission> permissions = systemPermissionRepository.findAllByOrderByCodeAsc();
        List<PermissionView> result = new ArrayList<PermissionView>();
        for (SystemPermission permission : permissions) {
            result.add(new PermissionView(
                    permission.getId(),
                    permission.getCode(),
                    permission.getName(),
                    permission.getDescription()
            ));
        }
        return result;
    }

    @Transactional
    // 创建新角色，并同步角色权限关系。
    public RoleView createRole(
            Long operatorUserId,
            String operatorUsername,
            String remoteIp,
            String code,
            String name,
            String description,
            List<Long> permissionIds
    ) {
        String normalizedCode = normalizeRoleCode(code);
        if (systemRoleRepository.existsByCode(normalizedCode)) {
            throw new AuthException("Role code already exists.");
        }
        SystemRole role = new SystemRole();
        role.setCode(normalizedCode);
        role.setName(normalizeRoleName(name));
        role.setDescription(normalizeDescription(description));
        role.setBuiltIn(false);
        SystemRole savedRole = systemRoleRepository.save(role);
        syncRolePermissions(savedRole, permissionIds);
        RoleView roleView = toRoleView(savedRole);
        operationLogService.record(
                operatorUserId,
                operatorUsername,
                "ADMIN",
                "ROLE_CREATED",
                "ROLE",
                savedRole.getId(),
                true,
                "Created role " + savedRole.getCode() + ".",
                "permissions=" + joinValues(roleView.getPermissions()),
                remoteIp
        );
        return roleView;
    }

    @Transactional
    // 更新角色基础信息和权限列表。
    public RoleView updateRole(
            Long operatorUserId,
            String operatorUsername,
            String remoteIp,
            Long roleId,
            String name,
            String description,
            List<Long> permissionIds
    ) {
        SystemRole role = systemRoleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException("Role does not exist."));
        if (RoleCodes.ROOT.equals(role.getCode())) {
            throw new AuthException("ROOT role cannot be edited.");
        }
        role.setName(normalizeRoleName(name));
        role.setDescription(normalizeDescription(description));
        SystemRole savedRole = systemRoleRepository.save(role);
        syncRolePermissions(savedRole, permissionIds);
        RoleView roleView = toRoleView(savedRole);
        operationLogService.record(
                operatorUserId,
                operatorUsername,
                "ADMIN",
                "ROLE_UPDATED",
                "ROLE",
                savedRole.getId(),
                true,
                "Updated role " + savedRole.getCode() + ".",
                "permissions=" + joinValues(roleView.getPermissions()),
                remoteIp
        );
        return roleView;
    }

    @Transactional
    // 替换用户现有角色集合。
    public AdminUserView updateUserRoles(Long operatorUserId, String operatorUsername, String remoteIp, Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new AuthException("At least one role is required.");
        }
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User does not exist."));
        List<SystemRole> roles = systemRoleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new AuthException("Some roles do not exist.");
        }

        userRoleRepository.deleteAllByUserAccount_Id(userId);
        for (SystemRole role : roles) {
            UserRole assignment = new UserRole();
            assignment.setUserAccount(account);
            assignment.setRole(role);
            userRoleRepository.save(assignment);
        }
        AdminUserView adminUserView = buildAdminUserView(account);
        operationLogService.record(
                operatorUserId,
                operatorUsername,
                "ADMIN",
                "USER_ROLES_UPDATED",
                "USER",
                account.getId(),
                true,
                "Updated user roles.",
                "username=" + account.getUsername() + ", roles=" + joinValues(adminUserView.getRoles()),
                remoteIp
        );
        return adminUserView;
    }

    @Transactional
    // 更新用户账户状态，并阻止误伤 ROOT 账户。
    public AdminUserView updateUserStatus(
            Long operatorUserId,
            String operatorUsername,
            String remoteIp,
            Long userId,
            String accountStatus
    ) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User does not exist."));
        AccountStatus targetStatus = normalizeAccountStatus(accountStatus);
        if (userRoleRepository.existsByUserAccount_IdAndRole_Code(userId, RoleCodes.ROOT)
                && targetStatus != AccountStatus.ACTIVE) {
            throw new AuthException("ROOT account must remain active.");
        }
        account.setAccountStatus(targetStatus);
        userAccountRepository.save(account);
        AdminUserView adminUserView = buildAdminUserView(account);
        operationLogService.record(
                operatorUserId,
                operatorUsername,
                "ADMIN",
                "USER_STATUS_UPDATED",
                "USER",
                account.getId(),
                true,
                "Updated user account status.",
                "username=" + account.getUsername() + ", status=" + targetStatus.name(),
                remoteIp
        );
        return adminUserView;
    }

    @Transactional
    // 管理员创建新用户并分配角色。
    public AdminUserView createUser(
            Long operatorUserId,
            String operatorUsername,
            String remoteIp,
            String username,
            String email,
            String rawPassword,
            String displayName,
            String phoneNumber,
            List<Long> roleIds
    ) {
        String normalizedUsername = username == null ? "" : username.trim();
        if (normalizedUsername.length() < 6) {
            throw new AuthException("Username must be at least 6 characters.");
        }
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new AuthException("Email format is invalid.");
        }
        if (userAccountRepository.existsByUsername(normalizedUsername)) {
            throw new AuthException("Username already exists.");
        }
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new AuthException("Email already exists.");
        }

        UserAccount account = new UserAccount();
        account.setUsername(normalizedUsername);
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setAccountStatus(AccountStatus.ACTIVE);
        UserAccount savedAccount = userAccountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUserAccount(savedAccount);
        profile.setDisplayName(displayName == null || displayName.trim().isEmpty() ? normalizedUsername : displayName.trim());
        profile.setPhoneNumber(phoneNumber == null || phoneNumber.trim().isEmpty() ? null : phoneNumber.trim());
        userProfileRepository.save(profile);

        if (roleIds != null && !roleIds.isEmpty()) {
            List<SystemRole> roles = systemRoleRepository.findAllById(roleIds);
            for (SystemRole role : roles) {
                UserRole assignment = new UserRole();
                assignment.setUserAccount(savedAccount);
                assignment.setRole(role);
                userRoleRepository.save(assignment);
            }
        } else {
            SystemRole defaultRole = systemRoleRepository.findByCode(RoleCodes.USER)
                    .orElseThrow(() -> new AuthException("Default user role is missing."));
            UserRole assignment = new UserRole();
            assignment.setUserAccount(savedAccount);
            assignment.setRole(defaultRole);
            userRoleRepository.save(assignment);
        }

        AdminUserView adminUserView = buildAdminUserView(savedAccount);
        operationLogService.record(
                operatorUserId,
                operatorUsername,
                "ADMIN",
                "USER_CREATED",
                "USER",
                savedAccount.getId(),
                true,
                "Created user account.",
                "username=" + normalizedUsername + ", email=" + normalizedEmail,
                remoteIp
        );
        return adminUserView;
    }

    @Transactional
    // 管理员重置指定用户的密码。
    public AdminUserView resetUserPassword(
            Long operatorUserId,
            String operatorUsername,
            String remoteIp,
            Long userId,
            String newPassword
    ) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User does not exist."));
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setFailedLoginAttempts(0);
        account.setLockedUntil(null);
        userAccountRepository.save(account);
        AdminUserView adminUserView = buildAdminUserView(account);
        operationLogService.record(
                operatorUserId,
                operatorUsername,
                "ADMIN",
                "USER_PASSWORD_RESET",
                "USER",
                account.getId(),
                true,
                "Reset user password.",
                "username=" + account.getUsername(),
                remoteIp
        );
        return adminUserView;
    }

    @Transactional
    // 删除用户及其关联数据，保留审计记录但脱敏。
    public void deleteUser(
            Long operatorUserId,
            String operatorUsername,
            String remoteIp,
            Long userId
    ) {
        if (operatorUserId.equals(userId)) {
            throw new AuthException("Cannot delete yourself.");
        }
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User does not exist."));
        if (userRoleRepository.existsByUserAccount_IdAndRole_Code(userId, RoleCodes.ROOT)) {
            throw new AuthException("Cannot delete ROOT account.");
        }

        loginAuditRepository.detachUserAccount(userId);
        operationLogRepository.detachOperatorUser(userId);
        entityManager.flush();
        entityManager.clear();
        userProfileRepository.findByUserAccount_Id(userId).ifPresent(userProfileRepository::delete);
        userRoleRepository.deleteAllByUserAccount_Id(userId);
        userAccountRepository.delete(account);

        operationLogService.record(
                operatorUserId,
                operatorUsername,
                "ADMIN",
                "USER_DELETED",
                "USER",
                userId,
                true,
                "Deleted user account.",
                "username=" + account.getUsername(),
                remoteIp
        );
    }

    @Transactional
    // 删除自定义角色及其权限关联。
    public void deleteRole(
            Long operatorUserId,
            String operatorUsername,
            String remoteIp,
            Long roleId
    ) {
        SystemRole role = systemRoleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException("Role does not exist."));
        if (role.isBuiltIn()) {
            throw new AuthException("Cannot delete built-in role.");
        }
        long assignedUsers = userRoleRepository.countByRole_Id(roleId);
        if (assignedUsers > 0) {
            throw new AuthException("Cannot delete role: " + assignedUsers + " user(s) are still assigned to it.");
        }
        rolePermissionRepository.deleteAllByRole_Id(roleId);
        systemRoleRepository.delete(role);
        operationLogService.record(
                operatorUserId,
                operatorUsername,
                "ADMIN",
                "ROLE_DELETED",
                "ROLE",
                roleId,
                true,
                "Deleted role.",
                "code=" + role.getCode(),
                remoteIp
        );
    }

    // 聚合资料、角色和权限，生成后台用户视图。
    private AdminUserView buildAdminUserView(UserAccount account) {
        UserProfile profile = userProfileRepository.findByUserAccount_Id(account.getId()).orElse(null);
        List<UserRole> userRoles = userRoleRepository.findAllByUserAccount_Id(account.getId());
        List<String> roles = new ArrayList<String>();
        List<Long> roleIds = new ArrayList<Long>();
        for (UserRole userRole : userRoles) {
            SystemRole role = safeResolveRole(userRole);
            if (role == null || role.getId() == null || role.getCode() == null || role.getCode().trim().isEmpty()) {
                continue;
            }
            roles.add(role.getCode());
            roleIds.add(role.getId());
        }

        Set<String> permissions = new LinkedHashSet<String>();
        if (!roleIds.isEmpty()) {
            List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRole_IdIn(roleIds);
            for (RolePermission rolePermission : rolePermissions) {
                SystemPermission permission = safeResolvePermission(rolePermission);
                if (permission == null || permission.getCode() == null || permission.getCode().trim().isEmpty()) {
                    continue;
                }
                permissions.add(permission.getCode());
            }
        }

        return new AdminUserView(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                resolveDisplayName(account, profile),
                resolveAccountStatus(account),
                roles,
                new ArrayList<String>(permissions),
                account.getLastLoginAt(),
                account.getLastLoginIp()
        );
    }

    // 列表查询时容忍个别脏数据，避免整页失败。
    private AdminUserView buildAdminUserViewSafely(UserAccount account) {
        try {
            return buildAdminUserView(account);
        } catch (RuntimeException exception) {
            return new AdminUserView(
                    account.getId(),
                    account.getUsername(),
                    account.getEmail(),
                    resolveDisplayName(account, null),
                    resolveAccountStatus(account),
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList(),
                    account.getLastLoginAt(),
                    account.getLastLoginIp()
            );
        }
    }

    private LoginAuditView toAuditView(LoginAudit audit) {
        return new LoginAuditView(
                audit.getId(),
                audit.getUserAccount() == null ? null : audit.getUserAccount().getId(),
                audit.getPrincipal(),
                audit.getRemoteIp(),
                audit.getUserAgent(),
                audit.isSuccess(),
                audit.getMessage(),
                splitSnapshot(audit.getRolesSnapshot()),
                splitSnapshot(audit.getPermissionsSnapshot()),
                audit.getLoggedInAt()
        );
    }

    private OperationLogView toOperationLogView(OperationLog log) {
        return new OperationLogView(
                log.getId(),
                log.getOperatorUser() == null ? null : log.getOperatorUser().getId(),
                log.getOperatorUsername(),
                log.getModuleCode(),
                log.getActionCode(),
                log.getTargetType(),
                log.getTargetId(),
                log.isSuccess(),
                log.getMessage(),
                log.getDetail(),
                log.getRemoteIp(),
                log.getOccurredAt()
        );
    }

    private RoleView toRoleView(SystemRole role) {
        List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRole_Id(role.getId());
        List<String> permissions = new ArrayList<String>();
        for (RolePermission rolePermission : rolePermissions) {
            SystemPermission permission = safeResolvePermission(rolePermission);
            if (permission == null || permission.getCode() == null || permission.getCode().trim().isEmpty()) {
                continue;
            }
            permissions.add(permission.getCode());
        }
        Collections.sort(permissions);
        return new RoleView(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isBuiltIn(),
                permissions
        );
    }

    // 用最新权限集替换角色当前的权限关联。
    private void syncRolePermissions(SystemRole role, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            rolePermissionRepository.deleteAllByRole_Id(role.getId());
            return;
        }
        Set<Long> uniquePermissionIds = new LinkedHashSet<Long>(permissionIds);
        List<SystemPermission> permissions = systemPermissionRepository.findAllByIdIn(uniquePermissionIds);
        if (permissions.size() != uniquePermissionIds.size()) {
            throw new AuthException("Some permissions do not exist.");
        }
        rolePermissionRepository.deleteAllByRole_Id(role.getId());
        for (SystemPermission permission : permissions) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
    }

    private List<String> splitSnapshot(String snapshot) {
        if (snapshot == null || snapshot.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] values = snapshot.split(",");
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            if (!value.trim().isEmpty()) {
                result.add(value.trim());
            }
        }
        return result;
    }

    private String normalizeRoleCode(String code) {
        String value = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        if (value.isEmpty()) {
            throw new AuthException("Role code is required.");
        }
        if (!value.matches("[A-Z0-9_]{2,48}")) {
            throw new AuthException("Role code must contain only uppercase letters, numbers or underscores.");
        }
        return value;
    }

    private String normalizeRoleName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) {
            throw new AuthException("Role name is required.");
        }
        if (value.length() > 100) {
            throw new AuthException("Role name must be at most 100 characters.");
        }
        return value;
    }

    private String normalizeDescription(String description) {
        String value = description == null ? "" : description.trim();
        if (value.length() > 255) {
            throw new AuthException("Role description must be at most 255 characters.");
        }
        return value.isEmpty() ? null : value;
    }

    private AccountStatus normalizeAccountStatus(String accountStatus) {
        AccountStatus status = AccountStatus.fromDatabaseValue(accountStatus);
        if (status == AccountStatus.UNKNOWN) {
            throw new AuthException("Account status is invalid.");
        }
        return status;
    }

    private String resolveDisplayName(UserAccount account, UserProfile profile) {
        if (profile != null && profile.getDisplayName() != null && !profile.getDisplayName().trim().isEmpty()) {
            return profile.getDisplayName().trim();
        }
        if (account.getUsername() != null && !account.getUsername().trim().isEmpty()) {
            return account.getUsername().trim();
        }
        return account.getEmail();
    }

    private String resolveAccountStatus(UserAccount account) {
        return account.getAccountStatus() == null ? "UNKNOWN" : account.getAccountStatus().name();
    }

    private SystemRole safeResolveRole(UserRole userRole) {
        try {
            return userRole == null ? null : userRole.getRole();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private SystemPermission safeResolvePermission(RolePermission rolePermission) {
        try {
            return rolePermission == null ? null : rolePermission.getPermission();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String normalizeFilter(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String joinValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(value.trim());
        }
        return builder.toString();
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= items.size()) {
            return new PageImpl<T>(Collections.<T>emptyList(), pageable, items.size());
        }
        int end = Math.min(start + pageable.getPageSize(), items.size());
        return new PageImpl<T>(items.subList(start, end), pageable, items.size());
    }
}
