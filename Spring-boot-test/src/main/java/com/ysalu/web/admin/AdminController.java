package com.ysalu.web.admin;

import com.ysalu.service.admin.AdminService;
import com.ysalu.service.admin.AdminUserView;
import com.ysalu.service.admin.LoginAuditAlertView;
import com.ysalu.service.admin.LoginAuditView;
import com.ysalu.service.admin.OperationLogView;
import com.ysalu.service.admin.PermissionView;
import com.ysalu.service.admin.RoleView;
import com.ysalu.service.security.PermissionCodes;
import com.ysalu.web.auth.SessionUser;
import com.ysalu.web.common.SessionAuthorization;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员控制器。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final SessionAuthorization sessionAuthorization;

    public AdminController(AdminService adminService, SessionAuthorization sessionAuthorization) {
        this.adminService = adminService;
        this.sessionAuthorization = sessionAuthorization;
    }

    @GetMapping("/users")
    public List<AdminUserView> listUsers(HttpSession session) {
        sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_USERS_READ);
        return adminService.listUsers();
    }

    @PutMapping("/users/{userId}/roles")
    public AdminUserView updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request,
            HttpSession session,
            HttpServletRequest httpServletRequest
    ) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_USERS_WRITE);
        return adminService.updateUserRoles(
                sessionUser.getId(),
                sessionUser.getUsername(),
                resolveClientIp(httpServletRequest),
                userId,
                request.getRoleIds()
        );
    }

    @PutMapping("/users/{userId}/status")
    public AdminUserView updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            HttpSession session,
            HttpServletRequest httpServletRequest
    ) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_USERS_WRITE);
        return adminService.updateUserStatus(
                sessionUser.getId(),
                sessionUser.getUsername(),
                resolveClientIp(httpServletRequest),
                userId,
                request.getAccountStatus()
        );
    }

    @GetMapping("/roles")
    public List<RoleView> listRoles(HttpSession session) {
        sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_ROLES_READ);
        return adminService.listRoles();
    }

    @PostMapping("/roles")
    public RoleView createRole(
            @Valid @RequestBody SaveRoleRequest request,
            HttpSession session,
            HttpServletRequest httpServletRequest
    ) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_ROLES_WRITE);
        return adminService.createRole(
                sessionUser.getId(),
                sessionUser.getUsername(),
                resolveClientIp(httpServletRequest),
                request.getCode(),
                request.getName(),
                request.getDescription(),
                request.getPermissionIds()
        );
    }

    @PutMapping("/roles/{roleId}")
    public RoleView updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody SaveRoleRequest request,
            HttpSession session,
            HttpServletRequest httpServletRequest
    ) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_ROLES_WRITE);
        return adminService.updateRole(
                sessionUser.getId(),
                sessionUser.getUsername(),
                resolveClientIp(httpServletRequest),
                roleId,
                request.getName(),
                request.getDescription(),
                request.getPermissionIds()
        );
    }

    @GetMapping("/permissions")
    public List<PermissionView> listPermissions(HttpSession session) {
        sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_PERMISSIONS_READ);
        return adminService.listPermissions();
    }

    @GetMapping("/login-audits")
    public Page<LoginAuditView> listLoginAudits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String principal,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) String remoteIp,
            HttpSession session
    ) {
        sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_LOGINS_READ);
        return adminService.listLoginAudits(page, size, principal, success, remoteIp);
    }

    @GetMapping("/login-audits/alerts")
    public LoginAuditAlertView getLoginAuditAlerts(HttpSession session) {
        sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_LOGINS_READ);
        return adminService.getLoginAuditAlerts();
    }

    @GetMapping("/operation-logs")
    public Page<OperationLogView> listOperationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operatorUsername,
            @RequestParam(required = false) Boolean success,
            HttpSession session
    ) {
        sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_OPERATION_LOGS_READ);
        return adminService.listOperationLogs(page, size, module, action, operatorUsername, success);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            String[] parts = forwardedFor.split(",");
            if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                return parts[0].trim();
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
