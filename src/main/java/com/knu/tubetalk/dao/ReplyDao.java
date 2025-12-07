package com.knu.tubetalk.dao;

import com.knu.tubetalk.domain.Reply;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReplyDao {

    private final DataSource dataSource;

    public ReplyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Reply reply) throws SQLException {
        String sql = "INSERT INTO REPLY " +
                "(Reply_id, Comment_id, User_id, Content, Created_at, Updated_at, " +
                "Like_count, Dislike_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reply.getReplyId());
            ps.setString(2, reply.getCommentId());
            ps.setString(3, reply.getUserId());
            ps.setString(4, reply.getContent());
            ps.setTimestamp(5, Timestamp.valueOf(reply.getCreatedAt()));
            if (reply.getUpdatedAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(reply.getUpdatedAt()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.setLong(7, reply.getLikeCount());
            ps.setLong(8, reply.getDislikeCount());

            ps.executeUpdate();
        }
    }

    public Optional<Reply> findById(String replyId) throws SQLException {
        String sql = "SELECT r.Reply_id, r.Comment_id, r.User_id, u.Login_id, r.Content, " +
                "r.Created_at, r.Updated_at, r.Like_count, r.Dislike_count " +
                "FROM REPLY r " +
                "INNER JOIN APP_USER u ON r.User_id = u.User_id " +
                "WHERE r.Reply_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, replyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reply reply = mapRowWithLoginId(rs);
                    return Optional.of(reply);
                }
                return Optional.empty();
            }
        }
    }

    public List<Reply> findByCommentId(String commentId) throws SQLException {
        String sql = "SELECT r.Reply_id, r.Comment_id, r.User_id, u.Login_id, r.Content, " +
                "r.Created_at, r.Updated_at, r.Like_count, r.Dislike_count " +
                "FROM REPLY r " +
                "INNER JOIN APP_USER u ON r.User_id = u.User_id " +
                "WHERE r.Comment_id = ? ORDER BY r.Created_at ASC";
        List<Reply> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, commentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowWithLoginId(rs));
                }
            }
        }
        return result;
    }

    public void updateContent(String replyId, String content, Timestamp updatedAt) throws SQLException {
        String sql = "UPDATE REPLY SET Content = ?, Updated_at = ? WHERE Reply_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, content);
            ps.setTimestamp(2, updatedAt);
            ps.setString(3, replyId);

            ps.executeUpdate();
        }
    }

    public void deleteById(String replyId) throws SQLException {
        String sql = "DELETE FROM REPLY WHERE Reply_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, replyId);
            ps.executeUpdate();
        }
    }

    public void updateLikeCount(String replyId, int delta) throws SQLException {
        String sql = "UPDATE REPLY SET Like_count = GREATEST(0, Like_count + ?) WHERE Reply_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setString(2, replyId);
            ps.executeUpdate();
        }
    }

    public void updateDislikeCount(String replyId, int delta) throws SQLException {
        String sql = "UPDATE REPLY SET Dislike_count = GREATEST(0, Dislike_count + ?) WHERE Reply_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setString(2, replyId);
            ps.executeUpdate();
        }
    }

    private Reply mapRow(ResultSet rs) throws SQLException {
        return new Reply(
                rs.getString("Reply_id"),
                rs.getString("Comment_id"),
                rs.getString("User_id"),
                rs.getString("Content"),
                rs.getTimestamp("Created_at").toLocalDateTime(),
                rs.getTimestamp("Updated_at") != null
                        ? rs.getTimestamp("Updated_at").toLocalDateTime()
                        : null,
                rs.getLong("Like_count"),
                rs.getLong("Dislike_count")
        );
    }
    
    private Reply mapRowWithLoginId(ResultSet rs) throws SQLException {
        return new Reply(
                rs.getString("Reply_id"),
                rs.getString("Comment_id"),
                rs.getString("User_id"),
                rs.getString("Login_id"),
                rs.getString("Content"),
                rs.getTimestamp("Created_at").toLocalDateTime(),
                rs.getTimestamp("Updated_at") != null
                        ? rs.getTimestamp("Updated_at").toLocalDateTime()
                        : null,
                rs.getLong("Like_count"),
                rs.getLong("Dislike_count")
        );
    }
    
    public long countAllReplies() throws SQLException {
        String sql = "SELECT COUNT(*) FROM REPLY";
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
