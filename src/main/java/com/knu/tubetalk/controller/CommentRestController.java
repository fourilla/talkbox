package com.knu.tubetalk.controller;

import com.knu.tubetalk.dto.CommentView;
import com.knu.tubetalk.dto.PageResponse;
import com.knu.tubetalk.domain.Reply;
import com.knu.tubetalk.domain.User;
import com.knu.tubetalk.domain.UserComment;
import com.knu.tubetalk.service.CommentService;
import com.knu.tubetalk.service.ReplyService;
import com.knu.tubetalk.service.ReactionService;
import com.knu.tubetalk.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:8080") // 개발용 CORS 허용
public class CommentRestController {

    private final CommentService commentService;
    private final UserService userService;
    private final ReplyService replyService;
    private final ReactionService reactionService;

    public CommentRestController(CommentService commentService, UserService userService, ReplyService replyService, ReactionService reactionService) {
        this.commentService = commentService;
        this.userService = userService;
        this.replyService = replyService;
        this.reactionService = reactionService;
    }

    @GetMapping("/thread/{threadId}")
    public ResponseEntity<PageResponse<CommentView>> getCommentsByThread(
            @PathVariable String threadId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "time") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {
        try {
            int size = 50; // 페이지당 50개 (댓글 + 답글 합쳐서)
            return ResponseEntity.ok(commentService.getCommentsWithReplies(threadId, page, size, sortBy, order));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/guestbook/{guestbookId}")
    public ResponseEntity<List<UserComment>> getCommentsByGuestbook(
            @PathVariable String guestbookId,
            @RequestParam(defaultValue = "time") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {
        try {
            return ResponseEntity.ok(commentService.getCommentsByGuestbook(guestbookId, sortBy, order));
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

            // 1. 로그인 아이디 가져오기
            String loginId = authentication.getName();

            // 2. [수정됨] DB에서 진짜 유저 정보 조회 (UUID 찾기)
            User user = userService.loadUserByLoginId(loginId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 사용자입니다.");
            }
            String realUserId = user.getUserId(); // 진짜 User_id (UUID)

            String content = requestData.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("댓글 내용이 비어있습니다.");
            }

            UserComment comment = new UserComment();
            // 3. [수정됨] 진짜 User_id를 넣어야 함 (loginId 아님!)
            comment.setUserId(realUserId); 
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
            @RequestBody Map<String, String> requestData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }
            
            // 댓글 조회
            UserComment comment = commentService.getComment(commentId);
            if (comment == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 작성자 확인
            String loginId = authentication.getName();
            User currentUser = userService.loadUserByLoginId(loginId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 사용자입니다.");
            }
            
            if (!comment.getUserId().equals(currentUser.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 댓글만 수정할 수 있습니다.");
            }
            
            String content = requestData.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("댓글 내용이 비어있습니다.");
            }
            
            commentService.updateComment(commentId, content, LocalDateTime.now());
            return ResponseEntity.ok("댓글이 수정되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("댓글 수정에 실패했습니다.");
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable String commentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }
            
            // 댓글 조회
            UserComment comment = commentService.getComment(commentId);
            if (comment == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 작성자 확인 또는 관리자 확인
            String loginId = authentication.getName();
            User currentUser = userService.loadUserByLoginId(loginId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 사용자입니다.");
            }
            
            // 관리자이거나 작성자인 경우에만 삭제 가능
            boolean isAdmin = "admin".equals(loginId);
            boolean isOwner = comment.getUserId().equals(currentUser.getUserId());
            
            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 댓글만 삭제할 수 있습니다.");
            }
            
            commentService.deleteComment(commentId);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("댓글 삭제에 실패했습니다.");
        }
    }

    // ========== 대댓글(Reply) API ==========
    
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<Reply>> getRepliesByComment(@PathVariable String commentId) {
        try {
            return ResponseEntity.ok(replyService.getRepliesByCommentId(commentId));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{commentId}/replies")
    public ResponseEntity<String> addReply(
            @PathVariable String commentId,
            @RequestBody Map<String, String> requestData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }
            
            String loginId = authentication.getName();
            User user = userService.loadUserByLoginId(loginId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 사용자입니다.");
            }
            
            String content = requestData.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("대댓글 내용이 비어있습니다.");
            }
            
            Reply reply = new Reply();
            reply.setUserId(user.getUserId());
            reply.setContent(content.trim());
            
            replyService.addReply(commentId, reply);
            return ResponseEntity.ok("대댓글이 등록되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("대댓글 등록에 실패했습니다.");
        }
    }

    @PutMapping("/replies/{replyId}")
    public ResponseEntity<String> updateReply(
            @PathVariable String replyId,
            @RequestBody Map<String, String> requestData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }
            
            Reply reply = replyService.getReply(replyId);
            if (reply == null) {
                return ResponseEntity.notFound().build();
            }
            
            String loginId = authentication.getName();
            User currentUser = userService.loadUserByLoginId(loginId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 사용자입니다.");
            }
            
            if (!reply.getUserId().equals(currentUser.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 대댓글만 수정할 수 있습니다.");
            }
            
            String content = requestData.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("대댓글 내용이 비어있습니다.");
            }
            
            replyService.updateReply(replyId, content, LocalDateTime.now());
            return ResponseEntity.ok("대댓글이 수정되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("대댓글 수정에 실패했습니다.");
        }
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<String> deleteReply(@PathVariable String replyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }
            
            Reply reply = replyService.getReply(replyId);
            if (reply == null) {
                return ResponseEntity.notFound().build();
            }
            
            String loginId = authentication.getName();
            User currentUser = userService.loadUserByLoginId(loginId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 사용자입니다.");
            }
            
            // 관리자이거나 작성자인 경우에만 삭제 가능
            boolean isAdmin = "admin".equals(loginId);
            boolean isOwner = reply.getUserId().equals(currentUser.getUserId());
            
            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 대댓글만 삭제할 수 있습니다.");
            }
            
            replyService.deleteReply(replyId);
            return ResponseEntity.ok("대댓글이 삭제되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("대댓글 삭제에 실패했습니다.");
        }
    }

    @PostMapping("/{targetId}/reaction")
    public ResponseEntity<Map<String, Object>> toggleReaction(
            @PathVariable String targetId,
            @RequestBody Map<String, String> requestData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String reactionTypeStr = requestData.get("reactionType");
            if (reactionTypeStr == null || (!reactionTypeStr.equals("L") && !reactionTypeStr.equals("D"))) {
                return ResponseEntity.badRequest().build();
            }
            
            char reactionType = reactionTypeStr.charAt(0);
            String loginId = authentication.getName();
            User currentUser = userService.loadUserByLoginId(loginId);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            reactionService.toggleReaction(currentUser.getUserId(), targetId, reactionType);
            
            // 업데이트된 좋아요/싫어요 수와 사용자의 현재 반응 반환
            Optional<Character> userReaction = reactionService.getUserReaction(currentUser.getUserId(), targetId);
            ReactionService.ReactionCounts counts = reactionService.getCounts(targetId);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("userReaction", userReaction.orElse(null));
            response.put("likeCount", counts.getLikeCount());
            response.put("dislikeCount", counts.getDislikeCount());
            
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{targetId}/reaction")
    public ResponseEntity<Map<String, Object>> getUserReaction(@PathVariable String targetId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.ok(Map.of("userReaction", null));
            }
            
            String loginId = authentication.getName();
            User currentUser = userService.loadUserByLoginId(loginId);
            if (currentUser == null) {
                return ResponseEntity.ok(Map.of("userReaction", null));
            }
            
            Optional<Character> userReaction = reactionService.getUserReaction(currentUser.getUserId(), targetId);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("userReaction", userReaction.orElse(null));
            
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
