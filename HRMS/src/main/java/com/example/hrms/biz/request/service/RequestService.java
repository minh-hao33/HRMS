package com.example.hrms.biz.request.service;

import com.example.hrms.biz.commoncode.notification.service.NotificationService;
import com.example.hrms.biz.request.model.Request;
import com.example.hrms.biz.request.model.criteria.RequestCriteria;
import com.example.hrms.biz.request.model.dto.RequestDto;
import com.example.hrms.biz.request.repository.RequestMapper;
import com.example.hrms.biz.user.model.User;
import com.example.hrms.biz.user.repository.UserMapper;
import com.example.hrms.common.http.criteria.Page;
import com.example.hrms.enumation.RequestStatusEnum;
import com.example.hrms.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RequestService {
    private final RequestMapper requestMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public RequestService(RequestMapper requestMapper, UserMapper userMapper, NotificationService notificationService) {
        this.requestMapper = requestMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    public int count(RequestCriteria criteria) {
        log.info("Counting requests with criteria: {}", criteria);
        return requestMapper.count(criteria);
    }

    public List<RequestDto.Resp> list(Page page, RequestCriteria criteria) {
        page.validate();
        log.info("Fetching request list with criteria: {}", criteria);
        try {
            List<Request> requests = requestMapper.select(
                    criteria.getRequestId(),
                    criteria.getUsername(),
                    criteria.getDepartmentId(),
                    criteria.getRequestType(),
                    criteria.getRequestReason(),
                    criteria.getRequestStatus(),
                    criteria.getApproverUsername(),
                    criteria.getStartTime(),
                    criteria.getEndTime()
            );
            log.info("Number of requests fetched: {}", requests.size());
            return requests.stream().map(RequestDto.Resp::toResponse).toList();
        } catch (Exception e) {
            log.error("Error fetching request list", e);
            throw new RuntimeException("Could not fetch request list, please try again later.");
        }
    }
    public int getTotalLeaveDays(String username) {
        log.info("Fetching total leave days for user: {}", username);
        Integer totalDays = requestMapper.calculateTotalLeaveDays(username);
        log.info("Total leave days for {}: {}", username, totalDays);
        return totalDays != null ? totalDays : 0;
    }
    public boolean createRequest(String username, RequestDto.Req requestDto) {
        Long departmentId = requestMapper.findDepartmentByUsername(username);
        if (departmentId == null) return false;

        String approverUsername = requestMapper.findLatestApproverByDepartment(departmentId);

        Request newRequest = new Request();
        newRequest.setUsername(username);
        newRequest.setDepartmentId(departmentId);
        newRequest.setRequestType(requestDto.getRequestType());
        newRequest.setRequestReason(requestDto.getRequestReason());
        newRequest.setRequestStatus(requestDto.getRequestStatus());
        newRequest.setApproverUsername(approverUsername);
        newRequest.setStartTime(requestDto.getStartTime());
        newRequest.setEndTime(requestDto.getEndTime());
        newRequest.setRejectionReason(null);  // 🔹 Đảm bảo rejectionReason là NULL khi tạo mới

        requestMapper.insertRequest(newRequest);
        // Send notification to the approver
        String requestTypeStr = requestDto.getRequestType() != null ?
            requestDto.getRequestType().toString() : "Request";

        if (approverUsername != null) {
            notificationService.createNotification(
                com.example.hrms.biz.commoncode.notification.model.Notification.builder()
                    .title("New Request Needs Approval")
                    .content("Employee " + username + " has submitted a new " + requestTypeStr +
                        " request that requires your approval.")
                    .sender(username)
                    .receiver(approverUsername)
                    .type("request")
                    .createdAt(LocalDateTime.now())
                    .build()
            );
        }

        // Send confirmation to the requester
        notificationService.createNotification(
            com.example.hrms.biz.commoncode.notification.model.Notification.builder()
                .title("Request Submitted")
                .content("Your " + requestTypeStr + " request has been submitted and is pending approval.")
                .sender("system")
                .receiver(username)
                .type("request")
                .createdAt(LocalDateTime.now())
                .build()
        );
        return true;
    }

    public boolean updateRequest(Request request) {
        if (request.getRequestStatus() == RequestStatusEnum.PENDING) {
            request.setRejectionReason(null);  // 🔹 Reset rejectionReason nếu trạng thái là PENDING
        }
        boolean updated = requestMapper.updateRequest(request) > 0;

        if (updated) {
            // Notify the approver about the update
            String approverUsername = request.getApproverUsername();
            if (approverUsername != null) {
                notificationService.createNotification(
                    com.example.hrms.biz.commoncode.notification.model.Notification.builder()
                        .title("Request Updated")
                        .content("Employee " + request.getUsername() + " has updated their request.")
                        .sender(request.getUsername())
                        .receiver(approverUsername)
                        .type("request")
                        .createdAt(LocalDateTime.now())
                        .build()
                );
            }
        }

        return updated;
    }

    public void deleteRequest(Long id) throws ResourceNotFoundException {
        Request request = requestMapper.findById(id);
        if (request == null) {
            throw new ResourceNotFoundException("Request not found for this id :: " + id);
        }
        int rowsAffected = requestMapper.deleteRequest(id);
        if (rowsAffected == 0) {
            throw new IllegalStateException("Cannot delete request with status REJECTED or APPROVED");
        }

        // Send notification to the approver
        String approverUsername = request.getApproverUsername();
        if (approverUsername != null) {
            notificationService.createNotification(
                com.example.hrms.biz.commoncode.notification.model.Notification.builder()
                    .title("Request Cancelled")
                    .content("Employee " + request.getUsername() + " has cancelled their request.")
                    .sender(request.getUsername())
                    .receiver(approverUsername)
                    .type("request")
                    .createdAt(LocalDateTime.now())
                    .build()
            );
        }
    }
    public List<RequestDto.Resp> getRequestsBySupervisor(String supervisorUsername) {
        log.info("Fetching requests for supervisor: {}", supervisorUsername);
        try {
            List<Request> requests = requestMapper.getRequestsBySupervisor(supervisorUsername);
            if (requests.isEmpty()) {
                log.warn("No requests found for supervisor: {}", supervisorUsername);
            } else {
                for (Request request : requests) {
                    log.info("Request: {}", request);
                }
            }
            return requests.stream().map(RequestDto.Resp::toResponse).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching requests", e);
            throw new RuntimeException("Could not fetch requests, please try again later.");
        }
    }

    // Xử lý approve/reject request
    public void approveOrRejectRequest(Long requestId, RequestStatusEnum requestStatus, String approverUsername, String rejectionReason) {
        if (requestStatus == RequestStatusEnum.REJECTED && (rejectionReason == null || rejectionReason.trim().isEmpty())) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting a request.");
        }

        // Get the request to know who submitted it
        Request request = requestMapper.findById(requestId);
        if (request == null) {
            throw new ResourceNotFoundException("Request not found for this id :: " + requestId);
        }

        int updatedRows = requestMapper.updateRequestStatus(requestId, requestStatus, approverUsername, rejectionReason);
        if (updatedRows == 0) {
            throw new RuntimeException("Request not found or already processed.");
        }

        // Send notification to the requester about the approval/rejection
        String title, content;
        if (requestStatus == RequestStatusEnum.APPROVED) {
            title = "Request Approved";
            content = "Your request has been approved by " + approverUsername;
        } else {
            title = "Request Rejected";
            content = "Your request has been rejected by " + approverUsername +
                (rejectionReason != null ? ". Reason: " + rejectionReason : "");
        }

        notificationService.createNotification(
            com.example.hrms.biz.commoncode.notification.model.Notification.builder()
                .title(title)
                .content(content)
                .sender(approverUsername)
                .receiver(request.getUsername())
                .type("request")
                .createdAt(LocalDateTime.now())
                .build()
        );
    }


}