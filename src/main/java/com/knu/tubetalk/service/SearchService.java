package com.knu.tubetalk.service;

import com.knu.tubetalk.dao.VideoDao;
import com.knu.tubetalk.domain.Video;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class SearchService {

    private final VideoDao videoDao;

    public SearchService(VideoDao videoDao) {
        this.videoDao = videoDao;
    }

    public List<Video> searchByTitle(String q) throws SQLException {
        return videoDao.findByTitleContaining(q);
    }
}
