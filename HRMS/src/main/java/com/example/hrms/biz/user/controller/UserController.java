package com.example.hrms.biz.user.controller;

import com.example.hrms.biz.booking.model.Booking;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    @RequestMapping("")
    public String openUserView(Model model) {
        model.addAttribute("user", new Booking());
        return "user";
    }
    @RequestMapping("/login")
    public String loginPage() {
        return "login"; // Trả về trang login.html
    }
    @RequestMapping("/home")
    public String homePage(Model model) {
        return "index";
    }
    @RequestMapping("/profile")
    public String profilePage() {
        return "profile";
    }
    @RequestMapping("/change-password")
    public String change() {
        return "change-password";
    }
}