package com.example.hrms.biz.department.controller.rest;

import com.example.hrms.biz.department.model.Department;
import com.example.hrms.biz.department.model.criteria.DepartmentCriteria;
import com.example.hrms.biz.department.model.dto.DepartmentDTO;
import com.example.hrms.biz.department.service.DepartmentService;
import com.example.hrms.common.http.model.Result;
import com.example.hrms.common.http.model.ResultData;
import com.example.hrms.common.http.model.ResultPageData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Department API v1")
@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentRestController {

    private final DepartmentService departmentService;

    public DepartmentRestController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "List departments")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get success",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DepartmentDTO.Resp.class)))
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content)})
    @GetMapping("")
    public ResultPageData<List<DepartmentDTO.Resp>> listDepartments(DepartmentCriteria criteria) {
        List<Department> departments = departmentService.listDepartment(criteria);
        List<DepartmentDTO.Resp> responseList = departments.stream()
                .map(dept -> new DepartmentDTO.Resp(
                        dept.getDepartmentId(),
                        dept.getDepartmentName(),
                        dept.getEmployeeName(),
                        dept.getRoleName(),
                        dept.getStatus() // Thêm trạng thái vào response
                ))
                .collect(Collectors.toList());

        ResultPageData<List<DepartmentDTO.Resp>> response = new ResultPageData<>(criteria, departments.size());
        response.setResultData(responseList);
        return response;
    }


    @Operation(summary = "Change department status")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department status changed",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class)) }),
            @ApiResponse(responseCode = "404", description = "Department not found",
                    content = @Content)
    })
    @PutMapping("/{id}/status")
    public Result changeDepartmentStatus(@PathVariable Long id, @RequestParam String status) {
        departmentService.changeDepartmentStatus(id, status);
        return new Result("Success", "Department status changed successfully.");
    }

    @Operation(summary = "Get department by ID")
    @GetMapping("/{id}")
    public ResultData<List<DepartmentDTO.Resp>> getDepartmentById(@PathVariable Long id) {
        List<Department> departments = departmentService.findById(id);
        List<DepartmentDTO.Resp> responseList = departments.stream()
                .map(DepartmentDTO.Resp::toResponse)
                .collect(Collectors.toList());
        ResultData<List<DepartmentDTO.Resp>> result = new ResultData<>();
        result.setResultData(responseList);
        return result;
    }

    @Operation(summary = "Create department")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Department created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class)) }),
            @ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content)
    })
    @PostMapping("")
    public Result createDepartment(@RequestBody DepartmentDTO.Req departmentReq) {
        departmentService.insert(departmentReq);
        return new Result("Success", "Department created successfully.");
    }

    @Operation(summary = "Update department")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class)) }),
            @ApiResponse(responseCode = "404", description = "Department not found",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public Result updateDepartment(@PathVariable Long id, @RequestBody DepartmentDTO.Req departmentReq) {
        departmentService.updateDepartment(id, departmentReq);
        return new Result("Success", "Department updated successfully.");
    }
}