package com.ysalu.service.overview;

import java.util.List;

// 项目概览。

public class ProjectOverview {

    private final String applicationName;
    private final String javaVersion;
    private final String message;
    private final List<String> modules;

    public ProjectOverview(String applicationName, String javaVersion, String message, List<String> modules) {
        this.applicationName = applicationName;
        this.javaVersion = javaVersion;
        this.message = message;
        this.modules = modules;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getModules() {
        return modules;
    }
}
