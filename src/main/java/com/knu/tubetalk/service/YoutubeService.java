package com.knu.tubetalk.service;

import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import com.knu.tubetalk.domain.Channel;
import com.knu.tubetalk.domain.Video;
import com.knu.tubetalk.exception.YoutubeApiException;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class YoutubeService {

    private static final String BASE_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos";
    private static final String BASE_CHANNEL_URL = "https://www.googleapis.com/youtube/v3/channels";
    private static final Gson GSON = new Gson();

    @Value("${youtube.api.key}")
    private String API_KEY;

    /**
     * Video ID로 비디오 + 채널 정보 모두 조회
     */
    public VideoAndChannel getVideoAndChannel(String videoId) throws YoutubeApiException {
        try {
            // 1. Video 정보 조회
            Map<String, Object> videoInfo = fetchVideoInfo(videoId);
            String channelId = (String) videoInfo.get("channelId");

            // 2. Channel 정보 조회
            Map<String, Object> channelInfo = getChannelInfo(channelId);

            // 3. Domain 객체 생성
            Video video = createVideoFromMap(videoInfo);
            Channel channel = createChannelFromMap(channelId, channelInfo);

            return new VideoAndChannel(video, channel);

        } catch (Exception e) {
            throw new YoutubeApiException("존재하지 않는 비디오 ID입니다: " + videoId);
        }
    }

    /**
     * Video API 호출
     */
    private Map<String, Object> fetchVideoInfo(String videoId) throws Exception {
        String charset = "UTF-8";
        String query = String.format(
            "part=snippet,statistics&id=%s&key=%s",
            URLEncoder.encode(videoId, charset),
            URLEncoder.encode(API_KEY, charset)
        );

        URL url = new URL(BASE_VIDEO_URL + "?" + query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), charset))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // JSON 파싱
            Map<String, Object> root = GSON.fromJson(sb.toString(), new TypeToken<Map<String, Object>>(){}.getType());
            List<Map<String, Object>> items = (List<Map<String, Object>>) root.get("items");

            if (items == null || items.isEmpty()) {
                throw new YoutubeApiException("Invalid video ID");
            }

            Map<String, Object> item = items.get(0);
            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
            Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");

            long likeCount = Long.parseLong((String) statistics.get("likeCount"));
            long commentCount = Long.parseLong((String) statistics.get("commentCount"));

            return Map.of(
                "videoId", videoId,
                "channelId", snippet.get("channelId"),
                "title", snippet.get("title"),
                "description", snippet.get("description"),
                "uploadedAt", snippet.get("publishedAt"),
                "likeCount", likeCount,
                "dislikeCount", -1L,  // YouTube API에서 제공 안 함
                "commentCount", commentCount
            );
        }
    }

    /**
     * Channel API 호출
     */
    private Map<String, Object> getChannelInfo(String channelId) throws Exception {
        String charset = "UTF-8";
        String apiUrl = BASE_CHANNEL_URL +
            "?part=snippet" +
            "&id=" + URLEncoder.encode(channelId, charset) +
            "&key=" + URLEncoder.encode(API_KEY, charset);

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            Map<String, Object> response = GSON.fromJson(sb.toString(), new TypeToken<Map<String, Object>>(){}.getType());
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

            if (items == null || items.isEmpty()) {
                throw new YoutubeApiException("Channel not found: " + channelId);
            }

            Map<String, Object> item = items.get(0);
            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");

            return Map.of(
                "channelTitle", snippet.get("title"),
                "channelDescription", snippet.get("description")
            );
        }
    }

    /**
     * Map → Video 객체 변환
     */
    private Video createVideoFromMap(Map<String, Object> videoInfo) {
        return new Video(
            (String) videoInfo.get("videoId"),
            (String) videoInfo.get("channelId"),
            (String) videoInfo.get("title"),
            (String) videoInfo.get("description"),
            parseDateTime((String) videoInfo.get("uploadedAt")),
            (Long) videoInfo.get("likeCount"),
            (Long) videoInfo.get("dislikeCount"),
            (Long) videoInfo.get("commentCount")
        );
    }

    /**
     * Map → Channel 객체 변환
     */
    private Channel createChannelFromMap(String channelId, Map<String, Object> channelInfo) {
        return new Channel(
            channelId,
            (String) channelInfo.get("channelTitle"),
            (String) channelInfo.get("channelDescription")
        );
    }

    /**
     * ISO 날짜 파싱
     */
    private LocalDateTime parseDateTime(String publishedAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return LocalDateTime.parse(publishedAt.replace("Z", "+00:00"), formatter);
    }

    /**
     * DTO: Video + Channel 함께 반환
     */
    public static class VideoAndChannel {
        private final Video video;
        private final Channel channel;

        public VideoAndChannel(Video video, Channel channel) {
            this.video = video;
            this.channel = channel;
        }

        public Video getVideo() { return video; }
        public Channel getChannel() { return channel; }
    }
}
