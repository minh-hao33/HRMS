package com.example.hrms.biz.role.service;

import com.example.hrms.biz.role.model.Role;
import com.example.hrms.biz.role.model.criteria.RoleCriteria;
import com.example.hrms.biz.role.model.dto.RoleDTO;
import com.example.hrms.biz.role.repository.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleMapper roleMapper;

    public RoleService(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public void insert(RoleDTO.Req req) {
        Role role = req.toRole();
        roleMapper.insertRole(role);
    }

    public void updateRole(Role role) {
        roleMapper.updateRole(role);
    }

    public void deleteRole(Long roleId) {
        roleMapper.deleteRole(roleId);
    }

    public int count(RoleCriteria criteria) {
        return roleMapper.count(criteria);
    }

    public List<RoleDTO.Resp> list(RoleCriteria criteria) {
        List<Role> roles = roleMapper.selectAll();
        return roles.stream().map(RoleDTO.Resp::toResponse).toList();
    }

    public List<Role> getAllRoles() {
        return roleMapper.selectAll();
    }
}