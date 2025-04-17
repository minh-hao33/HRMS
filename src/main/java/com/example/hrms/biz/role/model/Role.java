package com.example.hrms.biz.role.model;

import com.example.hrms.enumation.RoleEnum;
import lombok.Data;

@Data
public class Role {
    private Long roleId;
    private RoleEnum roleName;
}