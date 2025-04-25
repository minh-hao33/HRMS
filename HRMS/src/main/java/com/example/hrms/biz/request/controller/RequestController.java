package com.example.hrms.biz.request.controller;

import com.example.hrms.biz.request.model.Request;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/requests")
public class RequestController {


    @RequestMapping("")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'SUPERVISOR')")
    public String openRequestView(Model model, Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
            model.addAttribute("requests", new Request());
            return "requests";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SUPERVISOR"))) {
            model.addAttribute("request2", new Request());
            return "requests2";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("EMPLOYEE"))) {
            model.addAttribute("request11", new Request());
            return "request11";
        } else {
            // Handle other roles or unauthorized access
            return "accessDenied";
        }
    }


    @RequestMapping("/create")
    public String openCreate1(Model model) {
        model.addAttribute("create1", new Request());
        return "create1";
    }
    @RequestMapping("/update/{requestId}")
    public String openupdaterequest(Model model) {
        model.addAttribute("updaterequest", new Request());
        return "updaterequest";
    }
}