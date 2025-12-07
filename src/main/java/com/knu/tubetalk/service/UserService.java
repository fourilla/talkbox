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
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final GuestbookDao guestbookDao;
    
    // 이메일 형식 검증을 위한 정규식
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder, GuestbookDao guestbookDao) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.guestbookDao = guestbookDao;
    }
    
    /**
     * 이메일 형식이 유효한지 검증합니다.
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }
    
    /**
     * 아이디가 유효한지 검증합니다.
     */
    private void validateLoginId(String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }
        if (loginId.trim().length() < 3) {
            throw new IllegalArgumentException("아이디는 최소 3자 이상이어야 합니다.");
        }
    }

    public void register(JoinRequest dto) throws SQLException {
    	// 0. 입력값 검증
        validateLoginId(dto.getLoginId());
        validateEmail(dto.getEmail());
    	
    	// 1. loginId 중복 체크
        if (userDao.findByLoginId(dto.getLoginId().trim()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        // 2. email 중복 체크
        if (userDao.findByEmail(dto.getEmail().trim()).isPresent()) {
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
    
    public void updateUser(JoinRequest dto) throws SQLException {
        // 0. 입력값 검증
        validateLoginId(dto.getLoginId());
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            validateEmail(dto.getEmail());
        }
        
        // 1. 아이디로 기존 회원 정보 찾기
        User user = userDao.findByLoginId(dto.getLoginId().trim())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 정보 변경 (이메일 변경)
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String newEmail = dto.getEmail().trim();
            
            // 이메일이 변경되는 경우에만 중복 체크 (자기 자신의 이메일은 허용)
            if (!newEmail.equals(user.getEmail())) {
                if (userDao.findByEmail(newEmail).isPresent()) {
                    throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
                }
            }
            
            user.setEmail(newEmail);
        }

        // 3. 비밀번호 변경 (새 비밀번호가 입력된 경우에만 암호화해서 변경)
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 4. DB 업데이트 실행
        userDao.update(user);
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
    
    /**
     * 아이디 중복 여부를 확인합니다.
     */
    public boolean existsByLoginId(String loginId) throws SQLException {
        return userDao.findByLoginId(loginId).isPresent();
    }
    
    /**
     * 이메일 중복 여부를 확인합니다.
     * @param excludeLoginId 이 메일을 사용하는 사용자의 loginId를 제외하고 체크 (회원정보 수정 시 사용)
     */
    public boolean existsByEmail(String email, String excludeLoginId) throws SQLException {
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            return false;
        }
        
        // 자기 자신의 이메일은 중복이 아님
        if (excludeLoginId != null) {
            User user = userOpt.get();
            if (user.getLoginId().equals(excludeLoginId)) {
                return false;
            }
        }
        
        return true;
    }
    
}
