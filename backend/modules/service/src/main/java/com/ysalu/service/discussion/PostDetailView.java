package com.ysalu.service.discussion;

import java.time.LocalDateTime;
import java.util.List;

public class PostDetailView {

    private Long id;
    private String title;
    private String content;
    private String tags;
    private String authorName;
    private Long authorId;
    private long replyCount;
    private long likeCount;
    private long bookmarkCount;
    private boolean likedByCurrentUser;
    private boolean bookmarkedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReplyView> replies;

    public PostDetailView(Long id, String title, String content, String tags, String authorName, Long authorId, long replyCount, long likeCount, long bookmarkCount, boolean likedByCurrentUser, boolean bookmarkedByCurrentUser, LocalDateTime createdAt, LocalDateTime updatedAt, List<ReplyView> replies) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.authorName = authorName;
        this.authorId = authorId;
        this.replyCount = replyCount;
        this.likeCount = likeCount;
        this.bookmarkCount = bookmarkCount;
        this.likedByCurrentUser = likedByCurrentUser;
        this.bookmarkedByCurrentUser = bookmarkedByCurrentUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.replies = replies;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTags() { return tags; }
    public String getAuthorName() { return authorName; }
    public Long getAuthorId() { return authorId; }
    public long getReplyCount() { return replyCount; }
    public long getLikeCount() { return likeCount; }
    public long getBookmarkCount() { return bookmarkCount; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public boolean isBookmarkedByCurrentUser() { return bookmarkedByCurrentUser; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<ReplyView> getReplies() { return replies; }
}
