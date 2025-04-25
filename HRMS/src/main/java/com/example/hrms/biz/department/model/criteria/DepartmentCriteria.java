package com.example.hrms.biz.department.model.criteria;

import com.example.hrms.common.http.criteria.Page;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentCriteria extends Page {
    private Long departmentId;
    private String departmentName;
    private String status; // Thêm thuộc tính trạng thái

    public DepartmentCriteria() {}

    public DepartmentCriteria(Long departmentId, String departmentName, String status) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.status = status; // Khởi tạo thuộc tính trạng thái
    }
}