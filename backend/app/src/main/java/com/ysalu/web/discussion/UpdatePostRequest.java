package com.ysalu.web.discussion;

import javax.validation.constraints.Size;

public class UpdatePostRequest {

    @Size(max = 120)
    private String title;

    private String content;

    @Size(max = 255)
    private String tags;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
