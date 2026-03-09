package org.lcr.aidemo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// controller/HomeController.java
@Controller
public class HomeController {
    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user.getUsername());
        return "home";
    }

    @GetMapping("/admin/qamonitor")
    public String adminQaHome(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user.getUsername());
        return "admin-qa";
    }

    @GetMapping("/admin/kbmonitor")
    public String adminKbHome(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user.getUsername());
        return "admin-kb";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }
}