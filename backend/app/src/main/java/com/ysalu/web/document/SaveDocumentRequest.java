package com.ysalu.web.document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

// 保存文档请求对象。

public class SaveDocumentRequest {

    @NotBlank(message = "Document title is required.")
    @Size(max = 120, message = "Document title must be at most 120 characters.")
    private String title;

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
