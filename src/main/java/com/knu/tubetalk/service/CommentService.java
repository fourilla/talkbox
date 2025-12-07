package com.knu.tubetalk.service;

import com.knu.tubetalk.dao.UserCommentDao;
import com.knu.tubetalk.domain.UserComment;
import com.knu.tubetalk.dto.CommentView;
import com.knu.tubetalk.dto.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentService {

    private final UserCommentDao userCommentDao;

    public CommentService(UserCommentDao userCommentDao) {
        this.userCommentDao = userCommentDao;
    }

    public PageResponse<CommentView> getCommentsWithReplies(String threadId, int page, int size, String sortBy, String order) throws SQLException {
        
        long totalElements = userCommentDao.countAllByThreadId(threadId);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // 3. 현재 페이지에 해당하는 데이터 조회 (50개)
        List<CommentView> content = userCommentDao.findCommentsWithReplies(threadId, page, size, sortBy, order);
        
        // 4. 상자에 담아서 리턴
        return new PageResponse<>(content, page, totalPages, totalElements);
    }

    public List<UserComment> getCommentsByGuestbook(String guestbookId, String sortBy, String order) throws SQLException {
        return userCommentDao.findByGuestbookId(guestbookId, sortBy, order);
    }

    @Transactional
    public void addThreadComment(String threadId, UserComment comment) throws SQLException {
    	
        comment.setThreadId(threadId);
        comment.setCommentId(generateUniqueId());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setLikeCount(0);
        comment.setDislikeCount(0);
        // userId는 서비스 호출 시 설정하는 것으로 가정

        userCommentDao.save(comment);
    }

    @Transactional
    public void addGuestbookComment(String guestbookId, UserComment comment) throws SQLException {
        comment.setGuestbookId(guestbookId);
        comment.setCommentId(generateUniqueId());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setLikeCount(0);
        comment.setDislikeCount(0);

        userCommentDao.save(comment);
    }

    public UserComment getComment(String commentId) throws SQLException {
        Optional<UserComment> opt = userCommentDao.findById(commentId);
        return opt.orElse(null);
    }

    @Transactional
    public void updateComment(String commentId, String content, LocalDateTime updatedAt) throws SQLException {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        Timestamp ts = Timestamp.valueOf(updatedAt);
        userCommentDao.updateContent(commentId, content, ts);
    }

    @Transactional
    public void deleteComment(String commentId) throws SQLException {
        userCommentDao.deleteById(commentId);
    }

    private String generateUniqueId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 26);
    }
}
