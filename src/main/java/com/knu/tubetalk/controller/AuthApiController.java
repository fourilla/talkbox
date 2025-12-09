package com.knu.tubetalk.controller;

import java.sql.SQLException;
import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import com.knu.tubetalk.domain.User;
import com.knu.tubetalk.dto.JoinRequest;
import com.knu.tubetalk.service.UserService;

@RestController
public class AuthApiController {

    private final UserService userService;

    public AuthApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/join")
    public ResponseEntity<String> register(@RequestBody JoinRequest dto) {
        try {
            userService.register(dto);
            return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원가입에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/delete")
    public String deleteUser(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

        String loginId = user.getUsername();

        try {
            userService.deleteUserByLoginId(loginId);
            return "redirect:/logout";

        } catch (SQLException e) {
            return "redirect:/delete?error=db_error";

        } catch (IllegalArgumentException e) {
            return "redirect:/delete?error=not_found";
        }
    }
    
    @GetMapping("/update")
    public String updatePage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        // 1. 로그인한 아이디로 회원 정보 가져오기
        String loginId = principal.getName();
        User user;
        try {
        	user = userService.loadUserByLoginId(loginId);
        } catch(SQLException E) {
        	return "redirect:/delete?error=db_error";
        }
        
        // 2. 모델에 "user"라는 이름으로 담기 (이게 있어야 HTML에서 ${user}를 쓸 수 있음)
        model.addAttribute("user", user);
        
        return "update"; // update.html로 이동
    }
    
    @PutMapping("/api/user/update")
    public ResponseEntity<String> updateUserInfo(
            @RequestBody JoinRequest dto,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        
        // 보안 검사: 로그인한 사람과 수정하려는 ID가 같은지 확인
        if (!principal.getUsername().equals(dto.getLoginId())) {
            return ResponseEntity.status(403).body("본인의 정보만 수정할 수 있습니다.");
        }

        try {
            userService.updateUser(dto);
            return ResponseEntity.ok("회원정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("정보 수정 실패: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/api/user")
    public ResponseEntity<String> deleteAccount(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        String loginId = payload.get("loginId");

        // 보안 검사: 로그인한 사람과 삭제 대상이 같은지 확인
        if (principal == null || !principal.getUsername().equals(loginId)) {
            return ResponseEntity.status(403).body("본인 계정만 탈퇴할 수 있습니다.");
        }

        try {
            userService.deleteUserCompletelyByLoginId(loginId);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body("탈퇴 처리 중 오류가 발생했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/api/user/check-login-id")
    public ResponseEntity<Map<String, Boolean>> checkLoginId(@RequestParam String loginId) {
        try {
            boolean exists = userService.existsByLoginId(loginId);
            Map<String, Boolean> response = new java.util.HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/api/user/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) String excludeLoginId) {
        try {
            boolean exists = userService.existsByEmail(email, excludeLoginId);
            Map<String, Boolean> response = new java.util.HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
