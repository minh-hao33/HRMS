package com.example.hrms.biz.request.controller.Rest;

import com.example.hrms.biz.request.model.Request;
import com.example.hrms.biz.request.model.criteria.RequestCriteria;
import com.example.hrms.biz.request.model.dto.RequestDto;
import com.example.hrms.biz.request.service.RequestService;
import com.example.hrms.biz.user.model.User;
import com.example.hrms.biz.user.service.UserService;
import com.example.hrms.common.http.criteria.Page;
import com.example.hrms.common.http.model.Result;
import com.example.hrms.common.http.model.ResultData;
import com.example.hrms.common.http.model.ResultPageData;
import com.example.hrms.enumation.RequestStatusEnum;
import com.example.hrms.exception.ResourceNotFoundException;
import com.example.hrms.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name = "API requests")
@RestController
@RequestMapping("/api/v1/requests")
public class RequestRestController {
    private final RequestService requestService;
    private UserService userService;

    public RequestRestController(RequestService requestService, UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    @Operation(summary = "List requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get success",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RequestDto.Resp.class)))
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content)})
    @GetMapping("")
    public ResultPageData<List<RequestDto.Resp>> list(Page page, RequestCriteria criteria) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Nếu là ADMIN → trả về toàn bộ danh sách
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"))) {
            int total = requestService.count(criteria);
            ResultPageData<List<RequestDto.Resp>> response = new ResultPageData<>(criteria, total);
            response.setResultData(requestService.list(page, criteria));
            return response;
        }
        // Nếu là SUPERVISOR → trả về request của department của supervisor
        else if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("SUPERVISOR"))) {
            String supervisorUsername = SecurityUtils.getCurrentUsername();
            List<RequestDto.Resp> requests = requestService.getRequestsBySupervisor(supervisorUsername);
            ResultPageData<List<RequestDto.Resp>> response = new ResultPageData<>(criteria, requests.size());
            response.setResultData(requests);
            return response;
        }
        // Nếu không phải ADMIN hoặc SUPERVISOR → chỉ trả về request của user hiện tại
        else {
            String username = SecurityUtils.getCurrentUsername();
            criteria.setUsername(username); // Lọc theo username
            int total = requestService.count(criteria);
            ResultPageData<List<RequestDto.Resp>> response = new ResultPageData<>(criteria, total);
            response.setResultData(requestService.list(page, criteria));
            return response;
        }
    }
    @Operation(summary = "Approve or Reject a request with a reason for rejection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request approved or rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or already processed",
                    content = @Content)})

    @PreAuthorize("hasRole('SUPERVISOR')")
    @PostMapping("/{requestId}/approve-reject")
    public Result approveOrRejectRequest(
            @PathVariable Long requestId,
            @RequestParam RequestStatusEnum requestStatus,
            @RequestParam(required = false) String rejectionReason) {

        try {
            String supervisorUsername = SecurityUtils.getCurrentUsername();
            requestService.approveOrRejectRequest(requestId, requestStatus, supervisorUsername, rejectionReason);
            return new Result("Success", "Request processed successfully");
        } catch (Exception e) {
            return new Result("Failed", "Failed to process request: " + e.getMessage());
        }
    }

    @Operation(summary = "Get total leave days of a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total leave days",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content)})
    @GetMapping("/days-off")
    public ResultData<Long> getTotalLeaveDays() {
        String username = SecurityUtils.getCurrentUsername();
        long totalLeaveDays = requestService.getTotalLeaveDays(username);
        return new ResultData<>(Result.SUCCESS, "Total leave days retrieved successfully", totalLeaveDays);
    }

    @Operation(summary = "Create a new request")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/create")
    public Result createRequest(@RequestBody RequestDto.Req requestDto) {
        String username = SecurityUtils.getCurrentUsername();
        boolean success = requestService.createRequest(username, requestDto);

        if (!success) {
            return new Result("Failed", "Failed to create request");
        }

        return new Result("Success", "Request created successfully.");
    }
    @Operation(summary = "Update an existing request")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request updated successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public Result updateRequest(@PathVariable Long id, @RequestBody Request request) {
        request.setRequestId(id);
        requestService.updateRequest(request);
        return new Result("Success", "Request updated successfully.");
    }

    @Operation(summary = "Delete a request")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Request not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete request with status REJECTED or APPROVED")
    })
    @DeleteMapping("/{id}")
    public Result deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
        return new Result("Success", "Request deleted successfully");

    }
}