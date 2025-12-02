package com.knu.tubetalk.dao;

import com.knu.tubetalk.domain.Channel;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ChannelDao {

    private final DataSource dataSource;

    public ChannelDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Channel channel) throws SQLException {
        String sql = "INSERT INTO CHANNEL (Channel_id, Name, Description) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, channel.getChannelId());
            ps.setString(2, channel.getName());
            ps.setString(3, channel.getDescription()); // CLOB은 setString으로도 가능[web:86][web:89]
            ps.executeUpdate();
        }
    }

    public Optional<Channel> findById(String channelId) throws SQLException {
        String sql = "SELECT Channel_id, Name, Description FROM CHANNEL WHERE Channel_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, channelId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Channel channel = new Channel(
                            rs.getString("Channel_id"),
                            rs.getString("Name"),
                            rs.getString("Description")
                    );
                    return Optional.of(channel);
                }
                return Optional.empty();
            }
        }
    }

    public List<Channel> findAll() throws SQLException {
        String sql = "SELECT Channel_id, Name, Description FROM CHANNEL";
        List<Channel> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Channel channel = new Channel(
                        rs.getString("Channel_id"),
                        rs.getString("Name"),
                        rs.getString("Description")
                );
                result.add(channel);
            }
        }
        return result;
    }

    public void deleteById(String channelId) throws SQLException {
        String sql = "DELETE FROM CHANNEL WHERE Channel_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, channelId);
            ps.executeUpdate();
        }
    }
}
