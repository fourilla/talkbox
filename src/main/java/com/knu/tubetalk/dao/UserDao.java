package com.knu.tubetalk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.knu.tubetalk.domain.User;

@Repository
public class UserDao {

	private final DataSource dataSource;

	public UserDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Optional<User> findByLoginId(String loginId) throws SQLException {
		String sql = "SELECT User_id, Login_id, Email, Password FROM APP_USER WHERE Login_id = ?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, loginId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					User user = new User(rs.getString("User_id"), rs.getString("Login_id"), rs.getString("Email"),
							rs.getString("Password"));
					return Optional.of(user);
				}
				return Optional.empty();
			}
		}
	}
	
	public Optional<User> findByEmail(String email) throws SQLException {
	    String sql = "SELECT User_id, Login_id, Email, Password FROM APP_USER WHERE Email = ?";
	    try (Connection conn = dataSource.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, email);

	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                User user = new User(
	                        rs.getString("User_id"),
	                        rs.getString("Login_id"),
	                        rs.getString("Email"),
	                        rs.getString("Password")
	                );
	                return Optional.of(user);
	            }
	            return Optional.empty();
	        }
	    }
	}

	// 이건 필요하려나?
	public boolean existsByLoginId(String loginId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM APP_USER WHERE Login_id = ?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, loginId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
				return false;
			}
		}
	}

	public void save(User user) throws SQLException {
		String sql = "INSERT INTO APP_USER (User_id, Login_id, Email, Password) VALUES (?, ?, ?, ?)";
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, user.getUserId());
			ps.setString(2, user.getLoginId());
			ps.setString(3, user.getEmail());
			ps.setString(4, user.getPassword());
			ps.executeUpdate();
		}
	}
	
	public void update(User user) throws SQLException {
	    String sql = "UPDATE APP_USER SET Email = ?, Password = ? WHERE User_id = ?";
	    try (Connection conn = dataSource.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, user.getEmail());
	        ps.setString(2, user.getPassword());
	        ps.setString(3, user.getUserId());
	        
	        ps.executeUpdate();
	    }
	}

	public void deleteByUserId(String userId) throws SQLException {
		String sql = "DELETE FROM APP_USER WHERE User_id = ?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, userId);
			ps.executeUpdate();
		}
	}
	
	public long countAllUsers() throws SQLException {
		String sql = "SELECT COUNT(*) FROM APP_USER";
		try (Connection conn = dataSource.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql);
		     ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			return 0;
		}
	}
	
	public List<User> findAllUsers(int offset, int limit) throws SQLException {
		String sql = "SELECT User_id, Login_id, Email, Password FROM APP_USER ORDER BY Login_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		List<User> users = new ArrayList<>();
		try (Connection conn = dataSource.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, offset);
			ps.setInt(2, limit);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					users.add(new User(
						rs.getString("User_id"),
						rs.getString("Login_id"),
						rs.getString("Email"),
						rs.getString("Password")
					));
				}
			}
		}
		return users;
	}
}