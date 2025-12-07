package com.knu.tubetalk.dao;

import com.knu.tubetalk.domain.Video;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class VideoDao {

    private final DataSource dataSource;

    public VideoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Video video) throws SQLException {
        String sql = "INSERT INTO VIDEO " +
                "(Video_id, Channel_id, Title, Description, Uploaded_at, " +
                "Like_count, Dislike_count, Comment_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, video.getVideoId());
            ps.setString(2, video.getChannelId());
            ps.setString(3, video.getTitle());
            ps.setString(4, video.getDescription());
            ps.setTimestamp(5, Timestamp.valueOf(video.getUploadedAt())); // LocalDateTime 가정[web:88]
            ps.setLong(6, video.getLikeCount());
            ps.setLong(7, video.getDislikeCount());
            ps.setLong(8, video.getCommentCount());

            ps.executeUpdate();
        }
    }

    public Optional<Video> findById(String videoId) throws SQLException {
        String sql = "SELECT Video_id, Channel_id, Title, Description, Uploaded_at, " +
                "Like_count, Dislike_count, Comment_count " +
                "FROM VIDEO WHERE Video_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, videoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Video video = new Video(
                            rs.getString("Video_id"),
                            rs.getString("Channel_id"),
                            rs.getString("Title"),
                            rs.getString("Description"),
                            rs.getTimestamp("Uploaded_at").toLocalDateTime(),
                            rs.getLong("Like_count"),
                            rs.getLong("Dislike_count"),
                            rs.getLong("Comment_count")
                    );
                    return Optional.of(video);
                }
                return Optional.empty();
            }
        }
    }
    
    public List<Video> findByTitleContaining(String keyword) throws SQLException {
        String sql = "SELECT Video_id, Channel_id, Title, Description, Uploaded_at, " +
                     "Like_count, Dislike_count, Comment_count " +
                     "FROM VIDEO WHERE LOWER(Title) LIKE LOWER(?)";

        List<Video> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Video video = new Video(
                            rs.getString("Video_id"),
                            rs.getString("Channel_id"),
                            rs.getString("Title"),
                            rs.getString("Description"),
                            rs.getTimestamp("Uploaded_at").toLocalDateTime(),
                            rs.getLong("Like_count"),
                            rs.getLong("Dislike_count"),
                            rs.getLong("Comment_count")
                    );
                    result.add(video);
                }
            }
        }
        return result;
    }



    public List<Video> findByChannelId(String channelId) throws SQLException {
        String sql = "SELECT Video_id, Channel_id, Title, Description, Uploaded_at, " +
                "Like_count, Dislike_count, Comment_count " +
                "FROM VIDEO WHERE Channel_id = ?";
        List<Video> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, channelId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Video video = new Video(
                            rs.getString("Video_id"),
                            rs.getString("Channel_id"),
                            rs.getString("Title"),
                            rs.getString("Description"),
                            rs.getTimestamp("Uploaded_at").toLocalDateTime(),
                            rs.getLong("Like_count"),
                            rs.getLong("Dislike_count"),
                            rs.getLong("Comment_count")
                    );
                    result.add(video);
                }
            }
        }
        return result;
    }

    public void deleteById(String videoId) throws SQLException {
        String sql = "DELETE FROM VIDEO WHERE Video_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, videoId);
            ps.executeUpdate();
        }
    }
    
    public long countAllVideos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM VIDEO";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        }
    }
}
