package com.knu.tubetalk.controller;

import com.knu.tubetalk.domain.User;
import com.knu.tubetalk.domain.UserComment;
import com.knu.tubetalk.service.CommentService;
import com.knu.tubetalk.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:8080") // 개발용 CORS 허용
public class CommentRestController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentRestController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping("/thread/{threadId}")
    public ResponseEntity<List<UserComment>> getCommentsByThread(@PathVariable String threadId) {
        try {
            return ResponseEntity.ok(commentService.getCommentsByThread(threadId));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/guestbook/{guestbookId}")
    public ResponseEntity<List<UserComment>> getCommentsByGuestbook(@PathVariable String guestbookId) {
        try {
            return ResponseEntity.ok(commentService.getCommentsByGuestbook(guestbookId));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/thread/{threadId}")
    public ResponseEntity<String> addThreadComment(
            @PathVariable String threadId,
            @RequestBody Map<String, String> requestData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 로그인 안 된 경우 예외 처리
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            String loginId = authentication.getName();
            
         // 기존 UserService 메서드 사용!
            User user = userService.loadUserByLoginId(loginId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 사용자입니다.");
            }
            String userId = user.getUserId();  // DB의 실제 User_id

            String content = requestData.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("댓글 내용이 비어있습니다.");
            }

            UserComment comment = new UserComment();
            comment.setUserId(userId);
            comment.setContent(content.trim());

            commentService.addThreadComment(threadId, comment);
            return ResponseEntity.ok("댓글이 등록되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("댓글 등록에 실패했습니다.");
        }
    }

    @PostMapping("/guestbook/{guestbookId}")
    public ResponseEntity<String> addGuestbookComment(
            @PathVariable String guestbookId,
            @RequestBody Map<String, String> requestData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            String loginId = authentication.getName();

            String content = requestData.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("댓글 내용이 비어있습니다.");
            }

            UserComment comment = new UserComment();
            comment.setUserId(loginId);
            comment.setContent(content.trim());

            commentService.addGuestbookComment(guestbookId, comment);
            return ResponseEntity.ok("방명록 댓글이 등록되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("댓글 등록에 실패했습니다.");
        }
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<UserComment> getComment(@PathVariable String commentId) {
        try {
            UserComment comment = commentService.getComment(commentId);
            if (comment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(comment);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<String> updateComment(
            @PathVariable String commentId,
            @RequestBody UserComment updateData) {
        try {
            // TODO: 작성자만 수정 가능하도록 권한 체크 필요
            commentService.updateComment(commentId, updateData.getContent(), updateData.getUpdatedAt());
            return ResponseEntity.ok("댓글이 수정되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("댓글 수정에 실패했습니다.");
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable String commentId) {
        try {
            // TODO: 작성자만 삭제 가능하도록 권한 체크 필요
            commentService.deleteComment(commentId);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("댓글 삭제에 실패했습니다.");
        }
    }
}
