package com.ysalu.document;

import java.time.LocalDateTime;

/**
 * 文档版本视图。
 */
public class DocumentVersionView {

    private final Long id;
    private final int versionNumber;
    private final String title;
    private final String sourceType;
    private final LocalDateTime createdAt;

    public DocumentVersionView(Long id, int versionNumber, String title, String sourceType, LocalDateTime createdAt) {
        this.id = id;
        this.versionNumber = versionNumber;
        this.title = title;
        this.sourceType = sourceType;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getSourceType() {
        return sourceType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
