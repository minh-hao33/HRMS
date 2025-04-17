package com.example.hrms.biz.department.model.dto;

import com.example.hrms.biz.department.model.Department;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

public class DepartmentDTO {
    @Getter
    @Setter
    public static class Req {
        private Long departmentId;
        private String departmentName;
        private String employeeName;
        private String roleName;
        private String status; // Thêm thuộc tính trạng thái

        public Department toDepartment() {
            Department department = new Department();
            BeanUtils.copyProperties(this, department);
            return department;
        }
    }

    @Getter
    @Setter
    public static class Resp {
        private Long departmentId;
        private String departmentName;
        private String employeeName;
        private String roleName;
        private String status; // Thêm thuộc tính trạng thái

        public Resp() {}

        // Constructor để chấp nhận các giá trị null cho employeeName và roleName
        public Resp(Long departmentId, String departmentName, String employeeName, String roleName, String status) {
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.employeeName = employeeName;
            this.roleName = roleName;
            this.status = status; // Khởi tạo thuộc tính trạng thái
        }

        public static Resp toResponse(Department department) {
            Resp resp = new Resp();
            BeanUtils.copyProperties(department, resp);
            return resp;
        }
    }

}