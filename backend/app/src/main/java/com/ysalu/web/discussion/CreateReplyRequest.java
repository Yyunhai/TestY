package com.ysalu.web.discussion;

import javax.validation.constraints.NotBlank;

public class CreateReplyRequest {

    @NotBlank
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
