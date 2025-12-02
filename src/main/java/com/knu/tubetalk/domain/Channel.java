package com.knu.tubetalk.domain;

public class Channel {
    private String channelId;
    private String name;
    private String description;

    public Channel() {}

    public Channel(String channelId, String name, String description) {
        this.channelId = channelId;
        this.name = name;
        this.description = description;
    }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}