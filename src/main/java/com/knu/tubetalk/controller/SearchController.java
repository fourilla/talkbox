package com.knu.tubetalk.controller;

import com.knu.tubetalk.domain.Video;
import com.knu.tubetalk.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.SQLException;
import java.util.List;

@Controller
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public String search(String q, Model model) {
        try {
            List<Video> results = searchService.searchByTitle(q);

            model.addAttribute("query", q);
            model.addAttribute("results", results);

            return "search";

        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "검색 중 오류가 발생했습니다.");
            return "search";
        }
    }
}
