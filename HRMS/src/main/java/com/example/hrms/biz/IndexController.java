package com.example.hrms.biz;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class IndexController {

    @RequestMapping("")
    public String loginPage() {
        return "login"; // Trả về trang login.html
    }
}