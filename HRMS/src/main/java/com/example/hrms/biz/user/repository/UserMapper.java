package com.example.hrms.biz.user.repository;

import com.example.hrms.biz.user.model.User;
import com.example.hrms.biz.user.model.criteria.UserCriteria;
import com.example.hrms.enumation.RoleEnum;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // Lấy tất cả người dùng với thông tin phòng ban
    @Select("SELECT u.username, u.employee_name, u.password, u.department_id, " +
            "u.role_name, u.is_supervisor, u.status, u.email, d.department_name " +
            "FROM Users u LEFT JOIN Departments d ON u.department_id = d.department_id")
    @Results({
            @Result(property = "username", column = "username"),
            @Result(property = "employeeName", column = "employee_name"),
            @Result(property = "password", column = "password"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "roleName", column = "role_name", javaType = RoleEnum.class),
            @Result(property = "supervisor", column = "is_supervisor"),
            @Result(property = "status", column = "status"),
            @Result(property = "email", column = "email"),
            @Result(property = "departmentName", column = "department_name")
    })
    List<User> getAllUsers();

    // Lấy người dùng theo username với thông tin phòng ban
    @Select("SELECT u.username, u.employee_name, u.password, u.department_id, " +
            "u.role_name, u.is_supervisor, u.status, u.email, d.department_name " +
            "FROM Users u LEFT JOIN Departments d ON u.department_id = d.department_id " +
            "WHERE u.username = #{username}")
    @Results({
            @Result(property = "username", column = "username"),
            @Result(property = "employeeName", column = "employee_name"),
            @Result(property = "password", column = "password"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "roleName", column = "role_name", javaType = RoleEnum.class),
            @Result(property = "supervisor", column = "is_supervisor"),
            @Result(property = "status", column = "status"),
            @Result(property = "email", column = "email"),
            @Result(property = "departmentName", column = "department_name")
    })
    User getUserByUsername(String username);

    // Lấy mật khẩu theo username
    @Select("SELECT password FROM Users WHERE username = #{username}")
    String getPasswordByUsername(String username);

    // Thêm người dùng mới
    @Insert("INSERT INTO Users (username, employee_name, email, password, " +
            "department_id, role_name, is_supervisor, status) " +
            "VALUES (#{username}, #{employeeName}, #{email}, #{password}, " +
            "#{departmentId}, #{roleName}, #{isSupervisor}, #{status})")
    @Options(useGeneratedKeys = false, keyProperty = "username")
    int insertUser(User user);

    // Cập nhật thông tin người dùng
    @Update("UPDATE Users SET " +
            "employee_name = #{employeeName}, " +
            "email = #{email}, " +
            "password = #{password}, " +
            "department_id = #{departmentId}, " +
            "role_name = #{roleName}, " +
            "is_supervisor = #{isSupervisor}, " +
            "status = #{status} " +
            "WHERE username = #{username}")
    int updateUser(User user);

    // Xóa người dùng
    @Delete("DELETE FROM Users WHERE username = #{username}")
    int deleteUser(String username);

    // Tìm kiếm người dùng với bộ lọc
    @Select("<script>" +
            "SELECT u.username, u.employee_name, u.email, u.department_id, " +
            "u.role_name, u.is_supervisor, u.status, d.department_name " +
            "FROM Users u LEFT JOIN Departments d ON u.department_id = d.department_id " +
            "WHERE 1=1 " +
            "<if test='departmentIds != null and departmentIds.size() > 0'>" +
            "   AND u.department_id IN " +
            "   <foreach item='id' collection='departmentIds' open='(' separator=',' close=')'>" +
            "       #{id}" +
            "   </foreach>" +
            "</if>" +
            "<if test='roles != null and roles.size() > 0'>" +
            "   AND u.role_name IN " +
            "   <foreach item='role' collection='roles' open='(' separator=',' close=')'>" +
            "       #{role}" +
            "   </foreach>" +
            "</if>" +
            "</script>")
    @Results({
            @Result(property = "username", column = "username"),
            @Result(property = "employeeName", column = "employee_name"),
            @Result(property = "email", column = "email"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "roleName", column = "role_name", javaType = RoleEnum.class),
            @Result(property = "supervisor", column = "is_supervisor"),
            @Result(property = "status", column = "status"),
            @Result(property = "departmentName", column = "department_name")
    })
    List<User> searchUsers(@Param("departmentIds") List<Long> departmentIds,
                           @Param("roles") List<RoleEnum> roles);

    // Đếm số lượng người dùng theo tiêu chí
    @Select("<script>" +
            "SELECT COUNT(*) FROM Users WHERE 1=1 " +
            "<if test='departmentId != null'> AND department_id = #{departmentId} </if>" +
            "<if test='roleName != null'> AND role_name = #{roleName} </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "</script>")
    int count(UserCriteria criteria);


    // Kiểm tra username đã tồn tại chưa
    @Select("SELECT COUNT(*) FROM Users WHERE username = #{username}")
    int checkUsernameExists(String username);

    // Lấy department_id của supervisor
    @Select("SELECT department_id FROM Users WHERE username = #{supervisorUsername} AND is_supervisor = true")
    String getDepartmentIdBySupervisor(String supervisorUsername);

    // Lấy tất cả người dùng trong phòng ban
    @Select("SELECT u.username, u.employee_name, u.password, u.department_id, " +
            "u.role_name, u.is_supervisor, u.status, u.email, d.department_name " +
            "FROM Users u LEFT JOIN Departments d ON u.department_id = d.department_id " +
            "WHERE u.department_id = #{departmentId}")
    @Results({
            @Result(property = "username", column = "username"),
            @Result(property = "employeeName", column = "employee_name"),
            @Result(property = "password", column = "password"),
            @Result(property = "departmentId", column = "department_id"),
            @Result(property = "roleName", column = "role_name", javaType = RoleEnum.class),
            @Result(property = "supervisor", column = "is_supervisor"),
            @Result(property = "status", column = "status"),
            @Result(property = "email", column = "email"),
            @Result(property = "departmentName", column = "department_name")
    })
    List<User> getUsersByDepartment(String departmentId);
}