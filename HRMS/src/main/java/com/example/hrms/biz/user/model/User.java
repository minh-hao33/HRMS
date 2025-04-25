package com.example.hrms.biz.user.model;

import com.example.hrms.enumation.RoleEnum;
import lombok.Data;

@Data
public class User {
    private String username;
    private String employeeName;
    private String password;
    private Long departmentId;
    private RoleEnum roleName;
    private boolean isSupervisor;
    private String status;
    private String email;
    private String departmentName;

    public void setSupervisor(boolean isSupervisor) {
        this.isSupervisor = isSupervisor;
    }
    public void setRole_name(String roleName) {
        this.roleName = RoleEnum.fromString(roleName);
    }
}