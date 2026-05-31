package com.ysalu.config;

import com.ysalu.domain.auth.AccountStatus;
import com.ysalu.domain.auth.RolePermission;
import com.ysalu.domain.auth.SystemPermission;
import com.ysalu.domain.auth.SystemRole;
import com.ysalu.domain.auth.UserAccount;
import com.ysalu.domain.auth.UserProfile;
import com.ysalu.domain.auth.UserRole;
import com.ysalu.repository.auth.RolePermissionRepository;
import com.ysalu.repository.auth.SystemPermissionRepository;
import com.ysalu.repository.auth.SystemRoleRepository;
import com.ysalu.repository.auth.UserAccountRepository;
import com.ysalu.repository.auth.UserProfileRepository;
import com.ysalu.repository.auth.UserRoleRepository;
import com.ysalu.service.security.PermissionCodes;
import com.ysalu.service.security.RoleCodes;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
// 安全基础数据初始化器。
public class SecurityDataInitializer implements ApplicationRunner {

    private final SystemPermissionRepository systemPermissionRepository;
    private final SystemRoleRepository systemRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${testy.security.root.username:rootadmin}")
    private String rootUsername;

    @Value("${testy.security.root.email:root@testy.local}")
    private String rootEmail;

    @Value("${testy.security.root.password:Root@123456}")
    private String rootPassword;

    public SecurityDataInitializer(
            SystemPermissionRepository systemPermissionRepository,
            SystemRoleRepository systemRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.systemPermissionRepository = systemPermissionRepository;
        this.systemRoleRepository = systemRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, String> permissions = new LinkedHashMap<String, String>();
        permissions.put(PermissionCodes.OVERVIEW_READ, "View project overview");
        permissions.put(PermissionCodes.PROFILE_READ, "View own profile");
        permissions.put(PermissionCodes.DOCS_READ, "Read shared markdown documents");
        permissions.put(PermissionCodes.DOCS_WRITE, "Create and update own markdown documents");
        permissions.put(PermissionCodes.ADMIN_USERS_READ, "View user accounts");
        permissions.put(PermissionCodes.ADMIN_USERS_WRITE, "Change user roles and status");
        permissions.put(PermissionCodes.ADMIN_USERS_CREATE, "Create user accounts");
        permissions.put(PermissionCodes.ADMIN_USERS_DELETE, "Delete user accounts");
        permissions.put(PermissionCodes.ADMIN_USERS_RESET_PASSWORD, "Reset user passwords");
        permissions.put(PermissionCodes.ADMIN_ROLES_READ, "View system roles");
        permissions.put(PermissionCodes.ADMIN_ROLES_WRITE, "Create and update system roles");
        permissions.put(PermissionCodes.ADMIN_ROLES_DELETE, "Delete custom system roles");
        permissions.put(PermissionCodes.ADMIN_PERMISSIONS_READ, "View system permissions");
        permissions.put(PermissionCodes.ADMIN_LOGINS_READ, "View login audits");
        permissions.put(PermissionCodes.ADMIN_OPERATION_LOGS_READ, "View operation logs");
        permissions.put(PermissionCodes.SYSTEM_MANAGE, "Manage system settings");

        for (Map.Entry<String, String> entry : permissions.entrySet()) {
            ensurePermission(entry.getKey(), entry.getValue());
        }

        SystemRole rootRole = ensureRole(RoleCodes.ROOT, "Root Administrator", "Owns all project permissions");
        SystemRole userRole = ensureRole(RoleCodes.USER, "Standard User", "Default role for registered users");

        for (String permissionCode : permissions.keySet()) {
            ensureRolePermission(rootRole, permissionCode);
        }
        ensureRolePermission(userRole, PermissionCodes.OVERVIEW_READ);
        ensureRolePermission(userRole, PermissionCodes.PROFILE_READ);
        ensureRolePermission(userRole, PermissionCodes.DOCS_READ);
        ensureRolePermission(userRole, PermissionCodes.DOCS_WRITE);

        UserAccount rootAccount = userAccountRepository.findByUsername(rootUsername).orElseGet(this::createRootAccount);
        if (!userRoleRepository.existsByUserAccount_IdAndRole_Code(rootAccount.getId(), RoleCodes.ROOT)) {
            UserRole assignment = new UserRole();
            assignment.setUserAccount(rootAccount);
            assignment.setRole(rootRole);
            userRoleRepository.save(assignment);
        }
    }

    private SystemPermission ensurePermission(String code, String description) {
        return systemPermissionRepository.findByCode(code).orElseGet(() -> {
            SystemPermission permission = new SystemPermission();
            permission.setCode(code);
            permission.setName(code);
            permission.setDescription(description);
            return systemPermissionRepository.save(permission);
        });
    }

    private SystemRole ensureRole(String code, String name, String description) {
        return systemRoleRepository.findByCode(code).orElseGet(() -> {
            SystemRole role = new SystemRole();
            role.setCode(code);
            role.setName(name);
            role.setDescription(description);
            role.setBuiltIn(true);
            return systemRoleRepository.save(role);
        });
    }

    private void ensureRolePermission(SystemRole role, String permissionCode) {
        SystemPermission permission = systemPermissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new IllegalStateException("Permission missing: " + permissionCode));
        if (rolePermissionRepository.existsByRole_IdAndPermission_Id(role.getId(), permission.getId())) {
            return;
        }
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        rolePermissionRepository.save(rolePermission);
    }

    private UserAccount createRootAccount() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(rootUsername);
        userAccount.setEmail(rootEmail);
        userAccount.setPasswordHash(passwordEncoder.encode(rootPassword));
        userAccount.setAccountStatus(AccountStatus.ACTIVE);
        UserAccount savedAccount = userAccountRepository.save(userAccount);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserAccount(savedAccount);
        userProfile.setDisplayName("Root Administrator");
        userProfile.setPhoneNumber(null);
        userProfileRepository.save(userProfile);
        return savedAccount;
    }
}
