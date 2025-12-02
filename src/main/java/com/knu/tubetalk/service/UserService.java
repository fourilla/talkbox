package com.knu.tubetalk.service;

import com.knu.tubetalk.dao.UserDao;
import com.knu.tubetalk.dao.GuestbookDao;
import com.knu.tubetalk.domain.Guestbook;
import java.time.LocalDateTime;
import com.knu.tubetalk.domain.User;
import com.knu.tubetalk.dto.JoinRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

@Service
public class UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final GuestbookDao guestbookDao;

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder, GuestbookDao guestbookDao) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.guestbookDao = guestbookDao;
    }

    public void register(JoinRequest dto) throws SQLException {
    	
    	// 1. loginId 중복 체크
        if (userDao.findByLoginId(dto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        // 2. email 중복 체크
        if (userDao.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
    	
        String userId = UUID.randomUUID().toString().substring(0, 29);
        
        User user = new User(
                userId,
                dto.getLoginId(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword())
        );
        userDao.save(user);
        
        // 신규 사용자에 대한 방명록도 함께 생성
        Guestbook newGuestbook = new Guestbook(userId, LocalDateTime.now());
        guestbookDao.save(newGuestbook);
    }

    public User loadUserByLoginId(String loginId) throws SQLException {
        return userDao.findByLoginId(loginId).orElse(null);
    }

    public void deleteUser(String userId) throws SQLException {
        userDao.deleteByUserId(userId);
    }
    
    public void deleteUserByLoginId(String loginId) throws SQLException {
        // 1. loginId를 이용해 User 객체(User_id 포함)를 로드합니다.
        User user = userDao.findByLoginId(loginId)
                           .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 로드된 User_id로 삭제를 진행합니다.
        userDao.deleteByUserId(user.getUserId());
    }
    
    
}
