package com.knu.tubetalk.service;

import com.knu.tubetalk.dao.ReactionDao;
import com.knu.tubetalk.dao.UserCommentDao;
import com.knu.tubetalk.dao.ReplyDao;
import com.knu.tubetalk.domain.Reaction;
import com.knu.tubetalk.domain.UserComment;
import com.knu.tubetalk.domain.Reply;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReactionService {

    private final ReactionDao reactionDao;
    private final UserCommentDao userCommentDao;
    private final ReplyDao replyDao;
    private final CommentService commentService;
    private final ReplyService replyService;

    public ReactionService(ReactionDao reactionDao, UserCommentDao userCommentDao, ReplyDao replyDao, CommentService commentService, ReplyService replyService) {
        this.reactionDao = reactionDao;
        this.userCommentDao = userCommentDao;
        this.replyDao = replyDao;
        this.commentService = commentService;
        this.replyService = replyService;
    }

    @Transactional
    public void toggleReaction(String userId, String targetId, char reactionType) throws SQLException {
        Optional<Reaction> existingReaction = reactionDao.findByUserAndTarget(userId, targetId);

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            if (reaction.getReactionType() == reactionType) {
                // 같은 반응이면 삭제 (토글)
                reactionDao.delete(userId, targetId);
                updateCounts(targetId, -1, reactionType);
            } else {
                // 다른 반응이면 변경
                reactionDao.updateReactionType(userId, targetId, reactionType);
                updateCounts(targetId, -1, reaction.getReactionType());
                updateCounts(targetId, 1, reactionType);
            }
        } else {
            // 새로운 반응 추가
            Reaction newReaction = new Reaction(userId, targetId, reactionType, LocalDateTime.now());
            reactionDao.save(newReaction);
            updateCounts(targetId, 1, reactionType);
        }
    }

    private void updateCounts(String targetId, int delta, char reactionType) throws SQLException {
        // Comment인지 Reply인지 확인 - 실제로 존재하는지 확인
        UserComment comment = commentService.getComment(targetId);
        if (comment != null) {
            // Comment
            if (reactionType == 'L') {
                userCommentDao.updateLikeCount(targetId, delta);
            } else {
                userCommentDao.updateDislikeCount(targetId, delta);
            }
        } else {
            // Reply
            Reply reply = replyService.getReply(targetId);
            if (reply != null) {
                if (reactionType == 'L') {
                    replyDao.updateLikeCount(targetId, delta);
                } else {
                    replyDao.updateDislikeCount(targetId, delta);
                }
            } else {
                throw new IllegalArgumentException("Invalid target ID: " + targetId);
            }
        }
    }

    public Optional<Character> getUserReaction(String userId, String targetId) throws SQLException {
        Optional<Reaction> reaction = reactionDao.findByUserAndTarget(userId, targetId);
        return reaction.map(Reaction::getReactionType);
    }
    
    public ReactionCounts getCounts(String targetId) throws SQLException {
        UserComment comment = commentService.getComment(targetId);
        if (comment != null) {
            return new ReactionCounts(comment.getLikeCount(), comment.getDislikeCount());
        } else {
            Reply reply = replyService.getReply(targetId);
            if (reply != null) {
                return new ReactionCounts(reply.getLikeCount(), reply.getDislikeCount());
            } else {
                throw new IllegalArgumentException("Invalid target ID: " + targetId);
            }
        }
    }
    
    public static class ReactionCounts {
        private final long likeCount;
        private final long dislikeCount;
        
        public ReactionCounts(long likeCount, long dislikeCount) {
            this.likeCount = likeCount;
            this.dislikeCount = dislikeCount;
        }
        
        public long getLikeCount() { return likeCount; }
        public long getDislikeCount() { return dislikeCount; }
    }
}

