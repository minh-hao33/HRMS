package com.example.hrms.biz.role.repository;

import com.example.hrms.biz.role.model.Role;
import com.example.hrms.biz.role.model.criteria.RoleCriteria;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Insert("INSERT INTO Roles (role_id, role_name) VALUES (#{roleId}, #{roleName})")
    void insertRole(Role role);

    @Select("SELECT role_id AS roleId, role_name AS roleName FROM Roles")
    @Results({
            @Result(property = "roleId", column = "roleId"),
            @Result(property = "roleName", column = "roleName")
    })
    List<Role> selectAll();

    @Select("SELECT COUNT(*) FROM Roles")
    int count(RoleCriteria criteria);

    @Update("UPDATE Roles SET role_name = #{roleName} WHERE role_id = #{roleId}")
    void updateRole(Role role);

    @Delete("DELETE FROM Roles WHERE role_id = #{roleId}")
    void deleteRole(Long roleId);
}