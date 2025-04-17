package com.example.hrms.biz.request.model;

import com.example.hrms.enumation.RequestStatusEnum;
import com.example.hrms.enumation.RequestTypeEnum;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

@Data
@Getter
@Setter
public class Request {
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
}