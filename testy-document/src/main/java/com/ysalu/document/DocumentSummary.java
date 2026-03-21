package com.ysalu.document;

import java.time.LocalDateTime;

/**
 * 文档摘要视图。
 * 用于文档列表页展示标题、摘要和更新时间。
 */
public class DocumentSummary {

    private final Long id;
    private final String title;
    private final String excerpt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public DocumentSummary(Long id, String title, String excerpt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.excerpt = excerpt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
