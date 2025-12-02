package com.knu.tubetalk.domain;

import java.time.LocalDateTime;

public class Video {
    private String videoId;
    private String channelId;
    private String title;
    private String description;
    private LocalDateTime uploadedAt;
    private long likeCount;
    private long dislikeCount;
    private long commentCount;

    public Video() {}

    public Video(String videoId, String channelId, String title, String description,
                 LocalDateTime uploadedAt, long likeCount, long dislikeCount, long commentCount) {
        this.videoId = videoId;
        this.channelId = channelId;
        this.title = title;
        this.description = description;
        this.uploadedAt = uploadedAt;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
    }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }

    public long getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(long dislikeCount) { this.dislikeCount = dislikeCount; }

    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
}