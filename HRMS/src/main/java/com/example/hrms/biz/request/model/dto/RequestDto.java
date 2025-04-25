package com.example.hrms.biz.request.model.dto;

import com.example.hrms.biz.request.model.Request;
import com.example.hrms.enumation.RequestStatusEnum;
import com.example.hrms.enumation.RequestTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;
import java.util.Date;

public class RequestDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Req {
        private Long requestId;
        private String username;
        private Long departmentId;
        private RequestTypeEnum requestType;
        private String requestReason;
        private RequestStatusEnum requestStatus;
        private String approverUsername;
        private Date startTime;
        private Date endTime;
        private Timestamp createdAt;
        private Timestamp updatedAt;
        private Timestamp approvedAt;
        private String rejectionReason; // Thêm lý do từ chối

        public Request toRequest() {
            Request request = new Request();
            BeanUtils.copyProperties(this, request);
            return request;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Resp {
        private Long requestId;
        private String username;
        private Long departmentId;
        private RequestTypeEnum requestType;
        private String requestReason;
        private RequestStatusEnum requestStatus;
        private String approverUsername;
        private Date startTime;
        private Date endTime;
        private Timestamp createdAt;
        private Timestamp updatedAt;
        private Timestamp approvedAt;
        private String rejectionReason; // Thêm lý do từ chối

        public static Resp toResponse(Request request) {
            Resp resp = new Resp();
            BeanUtils.copyProperties(request, resp);
            return resp;
        }
    }
}