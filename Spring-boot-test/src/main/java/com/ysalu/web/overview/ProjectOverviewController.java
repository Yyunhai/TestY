package com.ysalu.web.overview;

import com.ysalu.service.security.PermissionCodes;
import com.ysalu.service.overview.ProjectOverview;
import com.ysalu.service.overview.ProjectOverviewService;
import com.ysalu.web.common.SessionAuthorization;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目概览控制器。
 * 登录用户具备概览权限后，可以查看当前后端模块与运行环境信息。
 */
@RestController
@RequestMapping("/api")
public class ProjectOverviewController {

    private final ProjectOverviewService projectOverviewService;
    private final SessionAuthorization sessionAuthorization;

    public ProjectOverviewController(ProjectOverviewService projectOverviewService, SessionAuthorization sessionAuthorization) {
        this.projectOverviewService = projectOverviewService;
        this.sessionAuthorization = sessionAuthorization;
    }

    /**
     * 返回系统概览数据。
     */
    @GetMapping("/overview")
    public ProjectOverview getOverview(HttpSession session) {
        sessionAuthorization.requirePermission(session, PermissionCodes.OVERVIEW_READ);
        return projectOverviewService.buildOverview();
    }
}
