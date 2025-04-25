package com.example.hrms.biz.user.controller;

import com.example.hrms.biz.booking.model.Booking;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class UserController {

    @RequestMapping("")
    public String openUserView(Model model) {
        model.addAttribute("user", new Booking());
        return "user";
    }

    @RequestMapping("/users/login")
    public String loginPage() {
        return "login"; // Trả về trang login.html
    }
    @RequestMapping("/users/home")
    public String homePage(Model model) {
        return "index";
    }
    @RequestMapping("/users/profile")
    public String profilePage() {
        return "profile";
    }
    @RequestMapping("/users/change-password")
    public String change() {
        return "change-password";
    }
}