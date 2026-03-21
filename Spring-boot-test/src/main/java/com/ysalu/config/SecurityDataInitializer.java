package com.ysalu.config;

import com.ysalu.domain.AccountStatus;
import com.ysalu.domain.RolePermission;
import com.ysalu.domain.SystemPermission;
import com.ysalu.domain.SystemRole;
import com.ysalu.domain.UserAccount;
import com.ysalu.domain.UserProfile;
import com.ysalu.domain.UserRole;
import com.ysalu.repository.RolePermissionRepository;
import com.ysalu.repository.SystemPermissionRepository;
import com.ysalu.repository.SystemRoleRepository;
import com.ysalu.repository.UserAccountRepository;
import com.ysalu.repository.UserProfileRepository;
import com.ysalu.repository.UserRoleRepository;
import com.ysalu.service.PermissionCodes;
import com.ysalu.service.RoleCodes;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统安全数据初始化器。
 * 应用启动时自动准备权限、角色以及 root 管理员账号，确保系统首次启动即可登录和鉴权。
 */
@Component
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

    /**
     * 初始化权限、角色及 root 管理员。
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, String> permissions = new LinkedHashMap<String, String>();
        permissions.put(PermissionCodes.OVERVIEW_READ, "View project overview");
        permissions.put(PermissionCodes.PROFILE_READ, "View own profile");
        permissions.put(PermissionCodes.DOCS_READ, "Read own markdown documents");
        permissions.put(PermissionCodes.DOCS_WRITE, "Create and update own markdown documents");
        permissions.put(PermissionCodes.ADMIN_USERS_READ, "View user accounts");
        permissions.put(PermissionCodes.ADMIN_LOGINS_READ, "View login audits");
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

    /**
     * 确保权限编码存在，不存在时自动补齐。
     */
    private SystemPermission ensurePermission(String code, String description) {
        return systemPermissionRepository.findByCode(code).orElseGet(() -> {
            SystemPermission permission = new SystemPermission();
            permission.setCode(code);
            permission.setName(code);
            permission.setDescription(description);
            return systemPermissionRepository.save(permission);
        });
    }

    /**
     * 确保角色存在，不存在时自动创建为内置角色。
     */
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

    /**
     * 为角色绑定权限，避免重复插入。
     */
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

    /**
     * 创建默认 root 管理员账号及其资料信息。
     */
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
