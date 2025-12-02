package com.knu.tubetalk.controller;

import com.knu.tubetalk.exception.YoutubeApiException;
import com.knu.tubetalk.service.ThreadService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;

@Controller
public class ThreadController {

    private final ThreadService threadService;

    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @GetMapping("/thread")
    public String threadPage(@RequestParam("v") String videoId, Model model) {
        try {
            ThreadService.ThreadPageData pageData = threadService.getOrCreateThreadData(videoId);

            model.addAttribute("video", pageData.getVideo());
            model.addAttribute("thread", pageData.getThread());

            return "thread";
        } catch (YoutubeApiException e) {
            model.addAttribute("errorMessage", "존재하지 않는 비디오 ID입니다: " + videoId);
            return "redirect:/main";
        } catch (SQLException e) {
            model.addAttribute("errorMessage", "데이터베이스 오류가 발생했습니다.");
            return "error";
        }
    }
}
