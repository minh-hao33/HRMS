package com.example.hrms.biz.user.model.dto;

import com.example.hrms.biz.department.model.Department;
import com.example.hrms.biz.department.model.dto.DepartmentDTO;
import com.example.hrms.biz.user.model.User;
import com.example.hrms.enumation.RoleEnum;
import lombok.Data;
import org.springframework.beans.BeanUtils;

public class UserDTO {

    @Data
    public static class Req {
        private String username;
        private String employeeName;
        private String password;
        private Long departmentId;
        private String departmentName;
        private RoleEnum roleName;
        private boolean isSupervisor;
        private String status;
        private String email;

        public User toUser() {
            User user = new User();
            BeanUtils.copyProperties(this, user);
            return user;
        }
    }

    @Data
    public static class Resp {
        private String username;
        private String employeeName;
        private Long departmentId;
        private String departmentName;
        private RoleEnum roleName;
        private boolean isSupervisor;
        private String status;
        private String email;
        public static UserDTO.Resp toResponse(User user) {
            UserDTO.Resp resp = new UserDTO.Resp();
            BeanUtils.copyProperties(user, resp);
            return resp;
        }

        public void setIsSupervisor(boolean isSupervisor) {
            this.isSupervisor = isSupervisor;
        }
    }
    @Data
    public static class DepartmentAndRole {
        private Long departmentId;
        private RoleEnum roleName;
    }
    @Data
    public static class UpdateReq {
        private String username;
        private String employeeName;
        private String password;
        private Long departmentId;
        private String departmentName;
        private RoleEnum roleName;
        private boolean isSupervisor;
        private String status;
        private String email;

    }
    @Data
    public static class ChangePasswordReq {
        private String oldPassword;
        private String newPassword;
    }

}