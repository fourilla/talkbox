package com.knu.tubetalk.controller;

import com.knu.tubetalk.domain.User;
import com.knu.tubetalk.domain.UserComment;
import com.knu.tubetalk.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {
    
    private final AdminService adminService;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    /**
     * 관리자 페이지 접근 권한 확인
     */
    private boolean checkAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            return false;
        }
        String loginId = authentication.getName();
        return adminService.isAdmin(loginId);
    }
    
    @GetMapping("/admin")
    public String adminPage(Model model) {
        if (!checkAdminAccess()) {
            return "redirect:/main?error=unauthorized";
        }
        
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            model.addAttribute("stats", stats);
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "통계를 불러오는 중 오류가 발생했습니다.");
        }
        
        return "admin";
    }
    
    @GetMapping("/api/admin/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        if (!checkAdminAccess()) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/api/admin/users")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!checkAdminAccess()) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            List<User> users = adminService.getUsers(page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("page", page);
            response.put("size", size);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/api/admin/comments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!checkAdminAccess()) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            List<UserComment> comments = adminService.getComments(page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("comments", comments);
            response.put("page", page);
            response.put("size", size);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/api/admin/users/{userId}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        if (!checkAdminAccess()) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }
        
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok("사용자가 삭제되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("삭제 중 오류가 발생했습니다.");
        }
    }
    
    @DeleteMapping("/api/admin/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<String> deleteComment(@PathVariable String commentId) {
        if (!checkAdminAccess()) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }
        
        try {
            adminService.deleteComment(commentId);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("삭제 중 오류가 발생했습니다.");
        }
    }
}

