package com.knu.tubetalk.dao;

import com.knu.tubetalk.domain.UserComment;
import com.knu.tubetalk.dto.CommentView;
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
    
    public List<CommentView> findCommentsWithReplies(String threadId, int page, int size) throws SQLException {
        int offset = (page - 1) * size;

        // UNION ALL을 사용하여 댓글과 대댓글을 합칩니다.
        // 정렬 기준(root_date): 대댓글도 부모 댓글의 작성 시간을 기준으로 정렬하기 위해 가져옵니다.
        String sql = 
            "SELECT * FROM ( " +
            "    SELECT " +
            "        c.Comment_id AS id, " +
            "        'COMMENT' AS type, " +
            "        c.Comment_id AS parent_id, " +
            "        c.Content, " +
            "        c.Created_at, " +
            "        c.User_id, " +
            "        u.Login_id, " +
            "        c.Like_count, " +
            "        c.Dislike_count, " +
            "        c.Created_at AS root_date " +  // 정렬용: 본인의 작성 시간
            "    FROM USER_COMMENT c " +
            "    JOIN APP_USER u ON c.User_id = u.User_id " +
            "    WHERE c.Thread_id = ? " +
            
            "    UNION ALL " +
            
            "    SELECT " +
            "        r.Reply_id AS id, " +
            "        'REPLY' AS type, " +
            "        r.Comment_id AS parent_id, " +
            "        r.Content, " +
            "        r.Created_at, " +
            "        r.User_id, " +
            "        u.Login_id, " +
            "        r.Like_count, " +
            "        r.Dislike_count, " +
            "        c.Created_at AS root_date " + // 정렬용: 부모 댓글의 작성 시간
            "    FROM REPLY r " +
            "    JOIN USER_COMMENT c ON r.Comment_id = c.Comment_id " +
            "    JOIN APP_USER u ON r.User_id = u.User_id " +
            "    WHERE c.Thread_id = ? " +
            ") " +
            // 정렬 순서: 1. 부모글 시간(최신순) 2. 부모글 먼저(type 'COMMENT' < 'REPLY') 3. 답글 시간(오래된순)
            "ORDER BY root_date DESC, type ASC, Created_at ASC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<CommentView> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, threadId);
            ps.setString(2, threadId);
            ps.setInt(3, offset);
            ps.setInt(4, size);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new CommentView(
                            rs.getString("id"),
                            rs.getString("type"),
                            rs.getString("parent_id"),
                            rs.getString("Content"),
                            rs.getString("User_id"),
                            rs.getString("Login_id"),
                            rs.getTimestamp("Created_at").toLocalDateTime(),
                            rs.getLong("Like_count"),
                            rs.getLong("Dislike_count")
                    ));
                }
            }
        }
        return result;
    }
    
    public long countAllByThreadId(String threadId) throws SQLException {
        // (댓글 개수) + (대댓글 개수) 를 더해서 가져옴
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM USER_COMMENT WHERE Thread_id = ?) + " +
                     "(SELECT COUNT(*) FROM REPLY r JOIN USER_COMMENT c ON r.Comment_id = c.Comment_id WHERE c.Thread_id = ?) " +
                     "FROM DUAL";
                     
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, threadId); // 첫 번째 ? (댓글 조건)
            ps.setString(2, threadId); // 두 번째 ? (대댓글 조건)
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1); // 합계 반환
                }
                return 0;
            }
        }
    }
    
    public List<UserComment> findByGuestbookId(String guestbookId) throws SQLException {
        String sql = "SELECT uc.Comment_id, uc.User_id, u.Login_id, uc.Thread_id, uc.Guestbook_id, uc.Content, " +
                "uc.Created_at, uc.Updated_at, uc.Like_count, uc.Dislike_count " +
                "FROM USER_COMMENT uc " +
                "INNER JOIN APP_USER u ON uc.User_id = u.User_id " +
                "WHERE uc.Guestbook_id = ? ORDER BY uc.Created_at ASC";
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
}
