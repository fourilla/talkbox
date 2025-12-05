package com.knu.tubetalk.dao;

import com.knu.tubetalk.domain.Reaction;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class ReactionDao {

    private final DataSource dataSource;

    public ReactionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Reaction reaction) throws SQLException {
        String sql = "INSERT INTO REACTION (User_id, Target_id, Reaction_type, Created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reaction.getUserId());
            ps.setString(2, reaction.getTargetId());
            ps.setString(3, String.valueOf(reaction.getReactionType()));
            ps.setTimestamp(4, Timestamp.valueOf(reaction.getCreatedAt()));

            ps.executeUpdate();
        }
    }

    public Optional<Reaction> findByUserAndTarget(String userId, String targetId) throws SQLException {
        String sql = "SELECT User_id, Target_id, Reaction_type, Created_at FROM REACTION WHERE User_id = ? AND Target_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, targetId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reaction reaction = new Reaction(
                            rs.getString("User_id"),
                            rs.getString("Target_id"),
                            rs.getString("Reaction_type").charAt(0),
                            rs.getTimestamp("Created_at").toLocalDateTime()
                    );
                    return Optional.of(reaction);
                }
                return Optional.empty();
            }
        }
    }

    public void delete(String userId, String targetId) throws SQLException {
        String sql = "DELETE FROM REACTION WHERE User_id = ? AND Target_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, targetId);

            ps.executeUpdate();
        }
    }

    public void updateReactionType(String userId, String targetId, char reactionType) throws SQLException {
        String sql = "UPDATE REACTION SET Reaction_type = ? WHERE User_id = ? AND Target_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(reactionType));
            ps.setString(2, userId);
            ps.setString(3, targetId);

            ps.executeUpdate();
        }
    }

    public long countLikes(String targetId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REACTION WHERE Target_id = ? AND Reaction_type = 'L'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, targetId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        }
    }

    public long countDislikes(String targetId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REACTION WHERE Target_id = ? AND Reaction_type = 'D'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, targetId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        }
    }
}

