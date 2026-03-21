package com.ysalu.web.document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 文档保存请求。
 * 创建和更新文档时共用这一份请求结构。
 */
public class SaveDocumentRequest {

    /**
     * 文档标题，不能为空，最长 120 个字符。
     */
    @NotBlank(message = "Document title is required.")
    @Size(max = 120, message = "Document title must be at most 120 characters.")
    private String title;

    /**
     * Markdown 文档正文，允许为空，长度上限 100000 个字符。
     */
    @Size(max = 100000, message = "Document content must be at most 100000 characters.")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
