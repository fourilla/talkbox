package com.knu.tubetalk.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.knu.tubetalk.domain.Channel;
import com.knu.tubetalk.domain.Video;
import com.knu.tubetalk.exception.YoutubeApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
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
     * Video ID → Video + Channel 정보 조회
     */
    public VideoAndChannel getVideoAndChannel(String videoId) throws YoutubeApiException {
        try {
            // 1. Video 정보 조회
            Map<String, Object> videoInfo = fetchVideoInfo(videoId);
            String channelId = (String) videoInfo.get("channelId");

            // 2. Channel 정보 조회
            Map<String, Object> channelInfo = getChannelInfo(channelId);

            // 3. Domain 생성
            Video video = createVideoFromMap(videoInfo);
            Channel channel = createChannelFromMap(channelId, channelInfo);

            return new VideoAndChannel(video, channel);

        } catch (YoutubeApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new YoutubeApiException(
                    "존재하지 않는 비디오 ID입니다: " + videoId + " - " + e.getMessage()
            );
        }
    }

    /**
     * Video API 호출 + safe 파싱 적용
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

            Map<String, Object> root = GSON.fromJson(sb.toString(), new TypeToken<Map<String, Object>>(){}.getType());
            List<Map<String, Object>> items = (List<Map<String, Object>>) root.get("items");

            if (items == null || items.isEmpty()) {
                throw new YoutubeApiException("Invalid video ID");
            }

            Map<String, Object> item = items.get(0);
            Map<String, Object> snippet = safeMap(item.get("snippet"));
            Map<String, Object> statistics = safeMap(item.get("statistics"));

            long likeCount = safeLong(statistics.get("likeCount"));
            long commentCount = safeLong(statistics.get("commentCount"));

            return Map.of(
                    "videoId", videoId,
                    "channelId", safeString(snippet.get("channelId")),
                    "title", safeString(snippet.get("title")),
                    "description", safeString(snippet.get("description")),
                    "uploadedAt", safeString(snippet.get("publishedAt")),
                    "likeCount", likeCount,
                    "dislikeCount", -1L,
                    "commentCount", commentCount
            );
        }
    }

    /**
     * Channel API 호출 + safe 파싱
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

            Map<String, Object> snippet = safeMap(items.get(0).get("snippet"));

            return Map.of(
                    "channelTitle", safeString(snippet.get("title")),
                    "channelDescription", safeString(snippet.get("description"))
            );
        }
    }

    /**
     * Map → Video 객체 변환
     */
    private Video createVideoFromMap(Map<String, Object> videoInfo) {
        return new Video(
                safeString(videoInfo.get("videoId")),
                safeString(videoInfo.get("channelId")),
                safeString(videoInfo.get("title")),
                safeString(videoInfo.get("description")),
                parseDateTime(safeString(videoInfo.get("uploadedAt"))),
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
                safeString(channelInfo.get("channelTitle")),
                safeString(channelInfo.get("channelDescription"))
        );
    }

    /**
     * ISO 날짜 파싱 (null 대비)
     */
    private LocalDateTime parseDateTime(String publishedAt) {
        try {
            if (publishedAt == null || publishedAt.isEmpty()) return LocalDateTime.now();
            return LocalDateTime.parse(publishedAt.replace("Z", "+00:00"), DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    /**
     * =============================
     *      SAFE PARSING UTILS
     * =============================
     */
    private long safeLong(Object value) {
        try {
            if (value == null) return 0L;
            if (value instanceof Number) return ((Number) value).longValue();
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }

    private Map<String, Object> safeMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Map.of();
    }

    /**
     * DTO 반환
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
