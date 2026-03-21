package com.ysalu.web;

import com.ysalu.service.AdminService;
import com.ysalu.service.AdminUserView;
import com.ysalu.service.LoginAuditView;
import com.ysalu.service.PermissionCodes;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员控制器。
 * 仅开放给具备管理员权限或 root 角色的用户，用于查看账号与登录审计信息。
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

    /**
     * 返回系统用户列表及其角色、权限、最近登录信息。
     */
    @GetMapping("/users")
    public List<AdminUserView> listUsers(HttpSession session) {
        sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_USERS_READ);
        return adminService.listUsers();
    }

    /**
     * 返回最近登录审计记录。
     */
    @GetMapping("/login-audits")
    public List<LoginAuditView> listLoginAudits(HttpSession session) {
        sessionAuthorization.requirePermission(session, PermissionCodes.ADMIN_LOGINS_READ);
        return adminService.listRecentLoginAudits();
    }
}
