package com.ysalu.service.discussion;

import java.time.LocalDateTime;

public class PostSummaryView {

    private Long id;
    private String title;
    private String tags;
    private String authorName;
    private long replyCount;
    private long likeCount;
    private long bookmarkCount;
    private boolean likedByCurrentUser;
    private boolean bookmarkedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostSummaryView(Long id, String title, String tags, String authorName, long replyCount, long likeCount, long bookmarkCount, boolean likedByCurrentUser, boolean bookmarkedByCurrentUser, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.tags = tags;
        this.authorName = authorName;
        this.replyCount = replyCount;
        this.likeCount = likeCount;
        this.bookmarkCount = bookmarkCount;
        this.likedByCurrentUser = likedByCurrentUser;
        this.bookmarkedByCurrentUser = bookmarkedByCurrentUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getTags() { return tags; }
    public String getAuthorName() { return authorName; }
    public long getReplyCount() { return replyCount; }
    public long getLikeCount() { return likeCount; }
    public long getBookmarkCount() { return bookmarkCount; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public boolean isBookmarkedByCurrentUser() { return bookmarkedByCurrentUser; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
