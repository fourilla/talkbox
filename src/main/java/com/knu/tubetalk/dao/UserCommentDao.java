package com.knu.tubetalk.dao;

import com.knu.tubetalk.domain.UserComment;
import com.knu.tubetalk.dto.CommentView;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    public List<CommentView> findCommentsWithReplies(String threadId, int page, int size, String sortBy, String order) throws SQLException {
        int offset = (page - 1) * size;

        String sortDirection = "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";
        String commentOrderBy;
        switch (sortBy.toLowerCase()) {
            case "like":
                commentOrderBy = "c.Like_count " + sortDirection + ", c.Created_at DESC";
                break;
            case "dislike":
                commentOrderBy = "c.Dislike_count " + sortDirection + ", c.Created_at DESC";
                break;
            case "time":
            default:
                commentOrderBy = "c.Created_at " + sortDirection;
                break;
        }

        // 1) 페이지에 해당하는 댓글만 조회
        String commentSql =
                "SELECT c.Comment_id AS id, 'COMMENT' AS type, c.Comment_id AS parent_id, " +
                "       c.Content, c.Created_at, c.User_id, u.Login_id, c.Like_count, c.Dislike_count " +
                "FROM USER_COMMENT c " +
                "JOIN APP_USER u ON c.User_id = u.User_id " +
                "WHERE c.Thread_id = ? " +
                "ORDER BY " + commentOrderBy + " " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<CommentView> comments = new ArrayList<>();
        List<String> commentIds = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(commentSql)) {

            ps.setString(1, threadId);
            ps.setInt(2, offset);
            ps.setInt(3, size);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CommentView comment = new CommentView(
                            rs.getString("id"),
                            rs.getString("type"),
                            rs.getString("parent_id"),
                            rs.getString("Content"),
                            rs.getString("User_id"),
                            rs.getString("Login_id"),
                            rs.getTimestamp("Created_at").toLocalDateTime(),
                            rs.getLong("Like_count"),
                            rs.getLong("Dislike_count")
                    );
                    comments.add(comment);
                    commentIds.add(comment.getId());
                }
            }
        }

        // 댓글이 없으면 바로 반환
        if (commentIds.isEmpty()) {
            return comments;
        }

        // 2) 해당 댓글들에 대한 답글 전체 조회 (작성 시간 오름차순)
        String placeholders = String.join(",", commentIds.stream().map(id -> "?").toArray(String[]::new));
        String replySql =
                "SELECT r.Reply_id AS id, 'REPLY' AS type, r.Comment_id AS parent_id, " +
                "       r.Content, r.Created_at, r.User_id, u.Login_id, r.Like_count, r.Dislike_count " +
                "FROM REPLY r " +
                "JOIN APP_USER u ON r.User_id = u.User_id " +
                "WHERE r.Comment_id IN (" + placeholders + ") " +
                "ORDER BY r.Created_at ASC";

        Map<String, List<CommentView>> repliesByParent = new HashMap<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(replySql)) {

            for (int i = 0; i < commentIds.size(); i++) {
                ps.setString(i + 1, commentIds.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CommentView reply = new CommentView(
                            rs.getString("id"),
                            rs.getString("type"),
                            rs.getString("parent_id"),
                            rs.getString("Content"),
                            rs.getString("User_id"),
                            rs.getString("Login_id"),
                            rs.getTimestamp("Created_at").toLocalDateTime(),
                            rs.getLong("Like_count"),
                            rs.getLong("Dislike_count")
                    );
                    repliesByParent.computeIfAbsent(reply.getParentId(), k -> new ArrayList<>()).add(reply);
                }
            }
        }

        // 3) 댓글 + 해당 댓글의 답글을 순서대로 합치기
        List<CommentView> combined = new ArrayList<>();
        for (CommentView comment : comments) {
            combined.add(comment);
            List<CommentView> replies = repliesByParent.get(comment.getId());
            if (replies != null) {
                combined.addAll(replies);
            }
        }

        return combined;
    }
    
    public long countAllByThreadId(String threadId) throws SQLException {
        // 페이지네이션은 "댓글" 개수를 기준으로 계산한다.
        String sql = "SELECT COUNT(*) FROM USER_COMMENT WHERE Thread_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, threadId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        }
    }
    
    public List<UserComment> findByGuestbookId(String guestbookId, String sortBy, String order) throws SQLException {
        // 정렬 기준 결정
        String orderByClause;
        String sortDirection = "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";
        
        switch (sortBy.toLowerCase()) {
            case "like":
                orderByClause = "uc.Like_count " + sortDirection;
                break;
            case "dislike":
                orderByClause = "uc.Dislike_count " + sortDirection;
                break;
            case "time":
            default:
                orderByClause = "uc.Created_at " + sortDirection;
                break;
        }
        
        String sql = "SELECT uc.Comment_id, uc.User_id, u.Login_id, uc.Thread_id, uc.Guestbook_id, uc.Content, " +
                "uc.Created_at, uc.Updated_at, uc.Like_count, uc.Dislike_count " +
                "FROM USER_COMMENT uc " +
                "INNER JOIN APP_USER u ON uc.User_id = u.User_id " +
                "WHERE uc.Guestbook_id = ? ORDER BY " + orderByClause;
        List<UserComment> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guestbookId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowWithLoginId(rs));
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

    public void updateLikeCount(String commentId, int delta) throws SQLException {
        String sql = "UPDATE USER_COMMENT SET Like_count = GREATEST(0, Like_count + ?) WHERE Comment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setString(2, commentId);
            ps.executeUpdate();
        }
    }

    public void updateDislikeCount(String commentId, int delta) throws SQLException {
        String sql = "UPDATE USER_COMMENT SET Dislike_count = GREATEST(0, Dislike_count + ?) WHERE Comment_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setString(2, commentId);
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
    
    private UserComment mapRowWithLoginId(ResultSet rs) throws SQLException {
        return new UserComment(
                rs.getString("Comment_id"),
                rs.getString("User_id"),
                rs.getString("Login_id"),
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
    
    public long countAllComments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM USER_COMMENT";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        }
    }
    
    public List<UserComment> findAllComments(int offset, int limit) throws SQLException {
        String sql = "SELECT uc.Comment_id, uc.User_id, u.Login_id, uc.Thread_id, uc.Guestbook_id, " +
                "uc.Content, uc.Created_at, uc.Updated_at, uc.Like_count, uc.Dislike_count " +
                "FROM USER_COMMENT uc " +
                "INNER JOIN APP_USER u ON uc.User_id = u.User_id " +
                "ORDER BY uc.Created_at DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<UserComment> comments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offset);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapRowWithLoginId(rs));
                }
            }
        }
        return comments;
    }
}

