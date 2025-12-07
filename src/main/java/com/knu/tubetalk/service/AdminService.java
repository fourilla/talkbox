package com.knu.tubetalk.service;

import com.knu.tubetalk.dao.UserDao;
import com.knu.tubetalk.dao.UserCommentDao;
import com.knu.tubetalk.dao.ReplyDao;
import com.knu.tubetalk.dao.VideoDao;
import com.knu.tubetalk.domain.User;
import com.knu.tubetalk.domain.UserComment;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    
    private static final String ADMIN_LOGIN_ID = "admin"; // 관리자 전용 아이디
    
    private final UserDao userDao;
    private final UserCommentDao userCommentDao;
    private final ReplyDao replyDao;
    private final VideoDao videoDao;
    
    public AdminService(UserDao userDao, UserCommentDao userCommentDao, 
                       ReplyDao replyDao, VideoDao videoDao) {
        this.userDao = userDao;
        this.userCommentDao = userCommentDao;
        this.replyDao = replyDao;
        this.videoDao = videoDao;
    }
    
    /**
     * 관리자 권한 확인
     */
    public boolean isAdmin(String loginId) {
        return ADMIN_LOGIN_ID.equals(loginId);
    }
    
    /**
     * 통계 대시보드 데이터 조회
     */
    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // 주요 통계 4개
        stats.put("totalUsers", userDao.countAllUsers());
        stats.put("totalComments", userCommentDao.countAllComments());
        stats.put("totalReplies", replyDao.countAllReplies());
        stats.put("totalVideos", videoDao.countAllVideos());
        
        return stats;
    }
    
    /**
     * 사용자 목록 조회
     */
    public List<User> getUsers(int page, int size) throws SQLException {
        int offset = (page - 1) * size;
        return userDao.findAllUsers(offset, size);
    }
    
    /**
     * 댓글 목록 조회
     */
    public List<UserComment> getComments(int page, int size) throws SQLException {
        int offset = (page - 1) * size;
        return userCommentDao.findAllComments(offset, size);
    }
    
    /**
     * 사용자 삭제
     */
    public void deleteUser(String userId) throws SQLException {
        userDao.deleteByUserId(userId);
    }
    
    /**
     * 댓글 삭제
     */
    public void deleteComment(String commentId) throws SQLException {
        userCommentDao.deleteById(commentId);
    }
}

