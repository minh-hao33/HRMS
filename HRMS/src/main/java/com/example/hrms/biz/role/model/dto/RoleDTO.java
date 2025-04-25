package com.example.hrms.biz.role.model.dto;

import com.example.hrms.biz.role.model.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

public class RoleDTO {
    @Getter
    @Setter
    public static class Req {
        private Long roleId;
        private String roleName;

        public Role toRole() {
            Role role = new Role();
            BeanUtils.copyProperties(this, role);
            return role;
        }
    }

    @Getter
    @Setter
    public static class Resp {
        private Long roleId;
        private String roleName;

        public static Resp toResponse(Role role) {
            Resp resp = new Resp();
            BeanUtils.copyProperties(role, resp);
            return resp;
        }
    }
}