package com.knu.tubetalk.domain;

import java.time.LocalDateTime;

public class Reaction {
    private String userId;
    private String targetId;  // Comment_id 또는 Reply_id
    private char reactionType; // 'L' = Like, 'D' = Dislike
    private LocalDateTime createdAt;

    public Reaction() {}

    public Reaction(String userId, String targetId, char reactionType, LocalDateTime createdAt) {
        this.userId = userId;
        this.targetId = targetId;
        this.reactionType = reactionType;
        this.createdAt = createdAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public char getReactionType() { return reactionType; }
    public void setReactionType(char reactionType) { this.reactionType = reactionType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

