package com.example.hrms.biz.department.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Department {
    private Long departmentId;
    private String departmentName;
    private String status; // Thêm thuộc tính trạng thái
}