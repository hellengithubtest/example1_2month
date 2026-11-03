package org.springsecurity.example.rest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedPageController {

    @GetMapping("/403")
    public String accessDenied(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("currentUser", authentication.getName());
        }
        return "access-denied";
    }
}
