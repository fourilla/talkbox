package com.knu.tubetalk.domain;

import java.time.LocalDateTime;

public class UserComment {
    private String commentId;
    private String userId;
    private String threadId;
    private String guestbookId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount;
    private long dislikeCount;

    public UserComment() {}

    public UserComment(String commentId, String userId, String threadId, String guestbookId,
                       String content, LocalDateTime createdAt, LocalDateTime updatedAt,
                       long likeCount, long dislikeCount) {
        this.commentId = commentId;
        this.userId = userId;
        this.threadId = threadId;
        this.guestbookId = guestbookId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public String getGuestbookId() { return guestbookId; }
    public void setGuestbookId(String guestbookId) { this.guestbookId = guestbookId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }

    public long getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(long dislikeCount) { this.dislikeCount = dislikeCount; }
}