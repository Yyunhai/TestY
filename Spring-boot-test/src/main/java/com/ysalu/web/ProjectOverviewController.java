package com.ysalu.web;

import com.ysalu.service.ProjectOverview;
import com.ysalu.service.ProjectOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
/**
 * 提供项目概览接口，供首页和调试页面展示基础信息。
 */
public class ProjectOverviewController {
    private final ProjectOverviewService projectOverviewService;

    public ProjectOverviewController(ProjectOverviewService projectOverviewService) {
        this.projectOverviewService = projectOverviewService;
    }

    @GetMapping("/overview")
    /**
     * 返回当前应用的概览信息。
     */
    public ProjectOverview getOverview() {
        return projectOverviewService.buildOverview();
    }
}
