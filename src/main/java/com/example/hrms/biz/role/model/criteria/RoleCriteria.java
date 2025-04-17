package com.example.hrms.biz.role.model.criteria;

import com.example.hrms.common.http.criteria.Page;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleCriteria extends Page {
    private Long roleId;
    private String roleName;

    public RoleCriteria() {}

    public RoleCriteria(Long roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }
}