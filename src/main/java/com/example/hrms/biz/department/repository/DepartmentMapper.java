package com.example.hrms.biz.department.repository;

import com.example.hrms.biz.department.model.Department;
import com.example.hrms.biz.department.model.criteria.DepartmentCriteria;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    @Insert("INSERT INTO Departments (department_name, status) VALUES (#{departmentName}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "departmentId")
    void insertDepartment(Department department);
    @Select("""
    SELECT 
        d.department_id AS departmentId, 
        d.department_name AS departmentName, 
        u.employee_name AS employeeName, 
        r.role_name AS roleName,
        d.status AS status
    FROM Departments d 
    LEFT JOIN Users u ON u.department_id = d.department_id 
    LEFT JOIN Roles r ON u.role_name = r.role_name 
    WHERE 
        (#{criteria.departmentName} IS NULL OR d.department_name LIKE CONCAT('%', #{criteria.departmentName}, '%'))
        AND (#{criteria.status} IS NULL OR d.status = #{criteria.status})
    ORDER BY d.department_name ASC, r.role_name ASC
""")
    List<Department> listDepartments(@Param("criteria") DepartmentCriteria criteria);


    @Update("UPDATE Departments SET status = #{status} WHERE department_id = #{departmentId}")
    void updateDepartmentStatus(@Param("departmentId") Long departmentId, @Param("status") String status);

    @Update("UPDATE Users SET employee_name = NULL, role_name = NULL WHERE department_id = #{departmentId}")
    void updateUsersToNull(Long departmentId);

    @Select("""
    SELECT 
        d.department_id AS departmentId, 
        d.department_name AS departmentName, 
        u.employee_name AS employeeName,
        r.role_name AS roleName 
    FROM Departments d 
    LEFT JOIN Users u ON d.department_id = u.department_id 
    LEFT JOIN Roles r ON u.role_name = r.role_name 
    WHERE d.department_id = #{id} 
    ORDER BY r.role_name ASC
""")
    List<Department> findById(Long id);

    @Select("SELECT COUNT(*) FROM Departments WHERE department_name = #{departmentName} AND (#{departmentId} IS NULL OR department_id != #{departmentId})")
    int countByName(@Param("departmentName") String departmentName, @Param("departmentId") Long departmentId);

    @Update("UPDATE Departments SET department_name = #{departmentName} WHERE department_id = #{departmentId}")
    void updateDepartment(Department department);
}