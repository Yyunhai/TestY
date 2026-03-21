package com.ysalu.service;

import com.ysalu.domain.LoginAudit;
import com.ysalu.domain.RolePermission;
import com.ysalu.domain.UserAccount;
import com.ysalu.domain.UserProfile;
import com.ysalu.domain.UserRole;
import com.ysalu.repository.LoginAuditRepository;
import com.ysalu.repository.RolePermissionRepository;
import com.ysalu.repository.UserAccountRepository;
import com.ysalu.repository.UserProfileRepository;
import com.ysalu.repository.UserRoleRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员服务。
 * 负责汇总用户、角色、权限和登录审计信息，供管理端接口调用。
 */
@Service
public class AdminService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final LoginAuditRepository loginAuditRepository;

    public AdminService(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            LoginAuditRepository loginAuditRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.loginAuditRepository = loginAuditRepository;
    }

    /**
     * 查询系统用户清单，并为每个用户补齐角色和权限信息。
     */
    @Transactional(readOnly = true)
    public List<AdminUserView> listUsers() {
        List<UserAccount> accounts = userAccountRepository.findAll();
        List<AdminUserView> result = new ArrayList<AdminUserView>();
        for (UserAccount account : accounts) {
            UserProfile profile = userProfileRepository.findByUserAccount_Id(account.getId()).orElse(null);
            List<UserRole> userRoles = userRoleRepository.findAllByUserAccount_Id(account.getId());
            List<String> roles = new ArrayList<String>();
            List<Long> roleIds = new ArrayList<Long>();
            for (UserRole userRole : userRoles) {
                roles.add(userRole.getRole().getCode());
                roleIds.add(userRole.getRole().getId());
            }

            Set<String> permissions = new LinkedHashSet<String>();
            if (!roleIds.isEmpty()) {
                List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRole_IdIn(roleIds);
                for (RolePermission rolePermission : rolePermissions) {
                    permissions.add(rolePermission.getPermission().getCode());
                }
            }

            result.add(new AdminUserView(
                    account.getId(),
                    account.getUsername(),
                    account.getEmail(),
                    profile == null ? account.getUsername() : profile.getDisplayName(),
                    account.getAccountStatus().name(),
                    roles,
                    new ArrayList<String>(permissions),
                    account.getLastLoginAt(),
                    account.getLastLoginIp()
            ));
        }
        return result;
    }

    /**
     * 查询最近的登录审计记录。
     */
    @Transactional(readOnly = true)
    public List<LoginAuditView> listRecentLoginAudits() {
        List<LoginAudit> audits = loginAuditRepository.findTop20ByOrderByLoggedInAtDesc();
        List<LoginAuditView> result = new ArrayList<LoginAuditView>();
        for (LoginAudit audit : audits) {
            result.add(new LoginAuditView(
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
            ));
        }
        return result;
    }

    /**
     * 将审计表里的逗号分隔快照恢复成列表结构。
     */
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
}
