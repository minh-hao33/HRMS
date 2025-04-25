package com.example.hrms.biz.request.model.criteria;

import com.example.hrms.common.http.criteria.Page;
import com.example.hrms.enumation.RequestStatusEnum;
import com.example.hrms.enumation.RequestTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class RequestCriteria extends Page {
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

    @Override
    public String toString() {
        return "RequestCriteria{" +
                "requestId=" + requestId +
                ", username='" + username + '\'' +
                ", departmentId=" + departmentId +
                ", requestType=" + requestType +
                ", requestReason='" + requestReason + '\'' +
                ", requestStatus=" + requestStatus +
                ", approverUsername='" + approverUsername + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", approvedAt=" + approvedAt +
                ", rejectionReason='" + rejectionReason + '\'' + // Hiển thị rejectionReason
                '}';
    }
}