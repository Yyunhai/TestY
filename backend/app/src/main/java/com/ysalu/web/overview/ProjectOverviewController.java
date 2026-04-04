package com.ysalu.web.overview;

import com.ysalu.service.security.PermissionCodes;
import com.ysalu.service.overview.ProjectOverview;
import com.ysalu.service.overview.ProjectOverviewService;
import com.ysalu.web.common.SessionAuthorization;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
// 项目概览接口控制器。
public class ProjectOverviewController {

    private final ProjectOverviewService projectOverviewService;
    private final SessionAuthorization sessionAuthorization;

    public ProjectOverviewController(ProjectOverviewService projectOverviewService, SessionAuthorization sessionAuthorization) {
        this.projectOverviewService = projectOverviewService;
        this.sessionAuthorization = sessionAuthorization;
    }

    @GetMapping("/overview")
    public ProjectOverview getOverview(HttpSession session) {
        sessionAuthorization.requirePermission(session, PermissionCodes.OVERVIEW_READ);
        return projectOverviewService.buildOverview();
    }
}
