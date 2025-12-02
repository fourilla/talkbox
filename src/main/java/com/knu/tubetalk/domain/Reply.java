package com.knu.tubetalk.domain;

import java.time.LocalDateTime;

public class Reply {
    private String replyId;
    private String commentId;
    private String userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount;
    private long dislikeCount;

    public Reply() {}

    public Reply(String replyId, String commentId, String userId, String content,
                 LocalDateTime createdAt, LocalDateTime updatedAt,
                 long likeCount, long dislikeCount) {
        this.replyId = replyId;
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }

    public String getReplyId() { return replyId; }
    public void setReplyId(String replyId) { this.replyId = replyId; }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

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