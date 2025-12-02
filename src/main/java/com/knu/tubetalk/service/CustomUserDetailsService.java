package com.knu.tubetalk.service;

import com.knu.tubetalk.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections; // 권한(Role) 정보가 없을 경우 빈 목록을 사용하기 위함

@Service // 🚨 Spring 빈으로 등록
public class CustomUserDetailsService implements UserDetailsService {

 private final UserService userService;

 // 🚨 UserService를 주입받습니다.
 public CustomUserDetailsService(UserService userService) {
     this.userService = userService;
 }

 /**
  * Spring Security의 핵심 메서드: 사용자 ID로 DB에서 User 정보를 로드합니다.
  */
 @Override
 public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
     try {
         // 1. UserService를 통해 DB에서 사용자 정보를 가져옵니다.
         User user = userService.loadUserByLoginId(loginId); 

         if (user == null) {
             throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId);
         }

         // 2. Spring Security가 사용하는 UserDetails 객체로 변환하여 반환합니다.
         // 현재는 권한 정보(Role)가 없으므로 빈 권한 목록을 넘깁니다.
         return new org.springframework.security.core.userdetails.User(
             user.getLoginId(), // Spring Security의 Username (여기서는 Login_id)
             user.getPassword(), // Spring Security의 Password (인코딩된 비밀번호)
             Collections.emptyList() // 권한(Authorities) 목록 (Role을 설정하지 않았다면 비워둡니다)
         );
     } catch (SQLException e) {
         // DB 접근 오류 발생 시 예외 처리
         throw new RuntimeException("DB에서 사용자 정보를 로드하는 중 오류가 발생했습니다.", e);
     }
 }
}