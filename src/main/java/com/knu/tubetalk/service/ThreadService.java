package com.knu.tubetalk.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knu.tubetalk.dao.ChannelDao;
import com.knu.tubetalk.dao.ThreadDao;
import com.knu.tubetalk.dao.VideoDao;
import com.knu.tubetalk.dto.TrendingThread;
import com.knu.tubetalk.domain.Channel;
import com.knu.tubetalk.domain.ThreadEntity;
import com.knu.tubetalk.domain.Video;
import com.knu.tubetalk.exception.YoutubeApiException;

@Service
public class ThreadService {

    private final VideoDao videoDao;
    private final ThreadDao threadDao;
    private final ChannelDao channelDao;
    private final YoutubeService youtubeService;

    public ThreadService(VideoDao videoDao, ThreadDao threadDao, ChannelDao channelDao, YoutubeService youtubeService) {
        this.videoDao = videoDao;
        this.threadDao = threadDao;
        this.channelDao = channelDao;
        this.youtubeService = youtubeService;
    }

    @Transactional
    public ThreadPageData getOrCreateThreadData(String videoId) throws SQLException, YoutubeApiException {
        Optional<Video> videoOpt = videoDao.findById(videoId);

        if (videoOpt.isPresent()) {
            Video video = videoOpt.get();
            ThreadEntity thread = threadDao.findById(videoId).orElse(null);
            return new ThreadPageData(video, thread);
        } else {
            // YouTube API 호출 → Video + Channel 정보
            YoutubeService.VideoAndChannel data = youtubeService.getVideoAndChannel(videoId);
            
            String channelId = data.getChannel().getChannelId();
            if (channelDao.findById(channelId).isEmpty()) {
                channelDao.save(data.getChannel());
            }

            videoDao.save(data.getVideo());
            
            ThreadEntity thread = new ThreadEntity(videoId, LocalDateTime.now(), 0L);
            threadDao.save(thread);

            return new ThreadPageData(data.getVideo(), thread);
        }
    }

    public static class ThreadPageData {
        private final Video video;
        private final ThreadEntity thread;

        public ThreadPageData(Video video, ThreadEntity thread) {
            this.video = video;
            this.thread = thread;
        }

        public Video getVideo() { return video; }
        public ThreadEntity getThread() { return thread; }
    }
    
    public List<TrendingThread> getTrendingThreads() throws SQLException {
        // 현재 시간으로부터 24시간 전 계산
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return threadDao.findTrending(yesterday);
    }
}
