package com.knu.tubetalk.controller;

import java.sql.SQLException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
// ...

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
}
