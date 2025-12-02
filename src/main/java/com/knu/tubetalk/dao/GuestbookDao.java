package com.knu.tubetalk.dao;

import com.knu.tubetalk.domain.Guestbook;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class GuestbookDao {

    private final DataSource dataSource;

    public GuestbookDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Guestbook guestbook) throws SQLException {
        String sql = "INSERT INTO GUESTBOOK (Guestbook_id, Created_at) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guestbook.getGuestbookId());
            ps.setTimestamp(2, Timestamp.valueOf(guestbook.getCreatedAt()));

            ps.executeUpdate();
        }
    }

    public Optional<Guestbook> findById(String guestbookId) throws SQLException {
        String sql = "SELECT Guestbook_id, Created_at FROM GUESTBOOK WHERE Guestbook_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guestbookId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Guestbook guestbook = new Guestbook(
                            rs.getString("Guestbook_id"),
                            rs.getTimestamp("Created_at").toLocalDateTime()
                    );
                    return Optional.of(guestbook);
                }
                return Optional.empty();
            }
        }
    }

    public void deleteById(String guestbookId) throws SQLException {
        String sql = "DELETE FROM GUESTBOOK WHERE Guestbook_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guestbookId);
            ps.executeUpdate();
        }
    }
}
