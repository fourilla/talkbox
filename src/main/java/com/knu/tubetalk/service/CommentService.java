package com.knu.tubetalk.service;

import com.knu.tubetalk.dao.UserCommentDao;
import com.knu.tubetalk.domain.UserComment;
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

    public List<UserComment> getCommentsByThread(String threadId) throws SQLException {
        return userCommentDao.findByThreadId(threadId);
    }

    public List<UserComment> getCommentsByGuestbook(String guestbookId) throws SQLException {
        return userCommentDao.findByGuestbookId(guestbookId);
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
