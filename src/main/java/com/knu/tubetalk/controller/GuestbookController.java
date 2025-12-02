package com.knu.tubetalk.controller;

import java.security.Principal;
import java.sql.SQLException;
import org.springframework.stereotype.Controller;
import com.knu.tubetalk.domain.User;
import com.knu.tubetalk.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

@Controller
public class GuestbookController {
	
	private final UserService userService;

    public GuestbookController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/guestbook")
    public String myGuestbook(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        return "redirect:/guestbook/" + principal.getName();
    }
    
	@GetMapping("/guestbook/{loginId}")
    public String viewGuestbook(@PathVariable String loginId, Model model) throws SQLException {
        User owner = userService.loadUserByLoginId(loginId);
        
        if (owner == null) {
            return "redirect:/main";
        }

        model.addAttribute("owner", owner);
        
        return "guestbook";
    }
}