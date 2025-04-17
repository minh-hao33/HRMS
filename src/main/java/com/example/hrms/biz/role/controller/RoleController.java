package com.example.hrms.biz.role.controller;

import com.example.hrms.biz.role.model.dto.RoleDTO;
import com.example.hrms.biz.role.model.criteria.RoleCriteria;
import com.example.hrms.biz.role.service.RoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("")
    public String openRoleView(Model model) {
        List<RoleDTO.Resp> roles = roleService.list(new RoleCriteria());
        model.addAttribute("roles", roles);
        return "role"; // Ensure this matches the name of your HTML file
    }
}