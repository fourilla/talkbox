package com.knu.tubetalk.dao;

import com.knu.tubetalk.domain.ThreadEntity;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class ThreadDao {

    private final DataSource dataSource;

    public ThreadDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(ThreadEntity thread) throws SQLException {
        String sql = "INSERT INTO THREAD (Thread_id, Created_at, Participant_count) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, thread.getThreadId());
            ps.setTimestamp(2, Timestamp.valueOf(thread.getCreatedAt()));
            ps.setLong(3, thread.getParticipantCount());

            ps.executeUpdate();
        }
    }

    public Optional<ThreadEntity> findById(String threadId) throws SQLException {
        String sql = "SELECT Thread_id, Created_at, Participant_count FROM THREAD WHERE Thread_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, threadId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ThreadEntity thread = new ThreadEntity(
                            rs.getString("Thread_id"),
                            rs.getTimestamp("Created_at").toLocalDateTime(),
                            rs.getLong("Participant_count")
                    );
                    return Optional.of(thread);
                }
                return Optional.empty();
            }
        }
    }

    public void updateParticipantCount(String threadId, long count) throws SQLException {
        String sql = "UPDATE THREAD SET Participant_count = ? WHERE Thread_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, count);
            ps.setString(2, threadId);
            ps.executeUpdate();
        }
    }

    public void deleteById(String threadId) throws SQLException {
        String sql = "DELETE FROM THREAD WHERE Thread_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, threadId);
            ps.executeUpdate();
        }
    }
}
