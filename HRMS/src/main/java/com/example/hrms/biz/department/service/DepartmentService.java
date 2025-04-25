package com.example.hrms.biz.department.service;

import com.example.hrms.biz.department.model.Department;
import com.example.hrms.biz.department.model.criteria.DepartmentCriteria;
import com.example.hrms.biz.department.model.dto.DepartmentDTO;
import com.example.hrms.biz.department.repository.DepartmentMapper;
import com.example.hrms.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public List<Department> findById(Long id) {
        List<Department> departments = departmentMapper.findById(id);
        if (departments.isEmpty()) {
            throw new RuntimeException("Department not found: " + id);
        }
        return departments;
    }

    public void insert(DepartmentDTO.Req req) {
        if (departmentMapper.countByName(req.getDepartmentName(), null) > 0) {
            throw new IllegalArgumentException("Department name already exists");
        }

        Department department = new Department();
        department.setDepartmentName(req.getDepartmentName());
        department.setStatus("Active"); // Đặt trạng thái mặc định là active
        departmentMapper.insertDepartment(department);

        if (department.getDepartmentId() == null) {
            throw new RuntimeException("Failed to insert department");
        }
    }

    public void updateDepartment(Long id, DepartmentDTO.Req req) {
        if (findById(id).isEmpty()) {
            throw new RuntimeException("Department not found");
        }
        if (departmentMapper.countByName(req.getDepartmentName(), id) > 0) {
            throw new RuntimeException("Department name already exists");
        }
        Department department = req.toDepartment();
        department.setDepartmentId(id);
        departmentMapper.updateDepartment(department);
    }

    public void changeDepartmentStatus(Long departmentId, String status) {
        if (findById(departmentId).isEmpty()) {
            throw new RuntimeException("Department not found");
        }

        if (!status.equals("Active") && !status.equals("Inactive")) {
            throw new IllegalArgumentException("Invalid status");
        }

        String currentStatus = departmentMapper.findStatusById(departmentId);

        if ("Active".equals(currentStatus) && "Inactive".equals(status)) {
            // Gỡ liên kết khi chuyển từ Active -> Inactive
            departmentMapper.unlinkUsersFromDepartment(departmentId);
        }

        departmentMapper.updateDepartmentStatus(departmentId, status);
    }


    public List<Department> listDepartment(DepartmentCriteria criteria) {
        return departmentMapper.listDepartments(criteria);
    }
}