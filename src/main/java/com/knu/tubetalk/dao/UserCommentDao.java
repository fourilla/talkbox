package com.knu.tubetalk.dao;

import com.knu.tubetalk.domain.UserComment;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserCommentDao {

    private final DataSource dataSource;

    public UserCommentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(UserComment comment) throws SQLException {
        String sql = "INSERT INTO USER_COMMENT " +
                "(Comment_id, User_id, Thread_id, Guestbook_id, Content, " +
                "Created_at, Updated_at, Like_count, Dislike_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, comment.getCommentId());
            ps.setString(2, comment.getUserId());
            ps.setString(3, comment.getThreadId());
            ps.setString(4, comment.getGuestbookId());
            ps.setString(5, comment.getContent());
            ps.setTimestamp(6, Timestamp.valueOf(comment.getCreatedAt()));
            if (comment.getUpdatedAt() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(comment.getUpdatedAt()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            ps.setLong(8, comment.getLikeCount());
            ps.setLong(9, comment.getDislikeCount());

            ps.executeUpdate();
        }
    }

    public Optional<UserComment> findById(String commentId) throws SQLException {
        String sql = "SELECT Comment_id, User_id, Thread_id, Guestbook_id, Content, " +
                "Created_at, Updated_at, Like_count, Dislike_count " +
                "FROM USER_COMMENT WHERE Comment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, commentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserComment comment = mapRow(rs);
                    return Optional.of(comment);
                }
                return Optional.empty();
            }
        }
    }

    public List<UserComment> findByThreadId(String threadId) throws SQLException {
        String sql = "SELECT Comment_id, User_id, Thread_id, Guestbook_id, Content, " +
                "Created_at, Updated_at, Like_count, Dislike_count " +
                "FROM USER_COMMENT WHERE Thread_id = ? ORDER BY Created_at ASC";
        List<UserComment> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, threadId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public List<UserComment> findByGuestbookId(String guestbookId) throws SQLException {
        String sql = "SELECT Comment_id, User_id, Thread_id, Guestbook_id, Content, " +
                "Created_at, Updated_at, Like_count, Dislike_count " +
                "FROM USER_COMMENT WHERE Guestbook_id = ? ORDER BY Created_at ASC";
        List<UserComment> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guestbookId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    public void updateContent(String commentId, String content, Timestamp updatedAt) throws SQLException {
        String sql = "UPDATE USER_COMMENT SET Content = ?, Updated_at = ? WHERE Comment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, content);
            ps.setTimestamp(2, updatedAt);
            ps.setString(3, commentId);

            ps.executeUpdate();
        }
    }

    public void deleteById(String commentId) throws SQLException {
        String sql = "DELETE FROM USER_COMMENT WHERE Comment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, commentId);
            ps.executeUpdate();
        }
    }

    private UserComment mapRow(ResultSet rs) throws SQLException {
        return new UserComment(
                rs.getString("Comment_id"),
                rs.getString("User_id"),
                rs.getString("Thread_id"),
                rs.getString("Guestbook_id"),
                rs.getString("Content"),
                rs.getTimestamp("Created_at").toLocalDateTime(),
                rs.getTimestamp("Updated_at") != null
                        ? rs.getTimestamp("Updated_at").toLocalDateTime()
                        : null,
                rs.getLong("Like_count"),
                rs.getLong("Dislike_count")
        );
    }
}
