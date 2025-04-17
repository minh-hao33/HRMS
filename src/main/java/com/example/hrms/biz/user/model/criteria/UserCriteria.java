package com.example.hrms.biz.user.model.criteria;

import com.example.hrms.common.http.criteria.Page;
import com.example.hrms.enumation.RoleEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserCriteria extends Page {
    private String username;
    private String employeeName;
    private Long departmentId;
    private String departmentName;
    private RoleEnum roleName;
    private Boolean isSupervisor;
    private String status;
    private String email;

    public UserCriteria(String username,String employeeName ,Long departmentId, String departmentName , RoleEnum roleName, Boolean isSupervisor, String status, String email) {
        this.username = username;
        this.employeeName = employeeName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.roleName = roleName;
        this.isSupervisor = isSupervisor;
        this.status = status;
        this.email = email;
    }

    public List<RoleEnum> getRoles() {
        return roleName != null ? List.of(roleName) : List.of();
    }

    public List<Long> getDepartmentIds() {
        return departmentId != null ? List.of(departmentId) : List.of();
    }
}