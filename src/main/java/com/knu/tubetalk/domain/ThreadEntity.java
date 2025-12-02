package com.knu.tubetalk.domain;

import java.time.LocalDateTime;

public class ThreadEntity {
    private String threadId;
    private LocalDateTime createdAt;
    private Long participantCount;

    public ThreadEntity() {}

    public ThreadEntity(String threadId, LocalDateTime createdAt, Long participantCount) {
        this.threadId = threadId;
        this.createdAt = createdAt;
        this.participantCount = participantCount;
    }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getParticipantCount() { return participantCount; }
    public void setParticipantCount(Long participantCount) { this.participantCount = participantCount; }
}