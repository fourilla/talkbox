package com.knu.tubetalk.domain;

import java.time.LocalDateTime;

public class Guestbook {
    private String guestbookId;
    private LocalDateTime createdAt;

    public Guestbook() {}

    public Guestbook(String guestbookId, LocalDateTime createdAt) {
        this.guestbookId = guestbookId;
        this.createdAt = createdAt;
    }

    public String getGuestbookId() { return guestbookId; }
    public void setGuestbookId(String guestbookId) { this.guestbookId = guestbookId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}