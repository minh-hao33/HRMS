package com.example.hrms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.example.hrms.biz.booking.repository",
        "com.example.hrms.biz.department.repository",
        "com.example.hrms.biz.user.repository",
        "com.example.hrms.biz.request.repository",
        "com.example.hrms.biz.role.repository",
        "com.example.hrms.biz.meetingroom.repository"})
public class HrmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsApplication.class, args);
    }

}
