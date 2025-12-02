package com.knu.tubetalk.domain;

public class User {
	private String userId;
	private String loginId;
	private String email;
	private String password;


	public User() {
	}

	public User(String userId, String loginId, String email, String password) {
		this.userId = userId;
		this.loginId = loginId;
		this.email = email;
		this.password = password;

	}

// getters / setters
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


}