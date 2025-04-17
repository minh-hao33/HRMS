package com.example.hrms.biz.role.controller.rest;

import com.example.hrms.biz.role.model.Role;
import com.example.hrms.biz.role.model.criteria.RoleCriteria;
import com.example.hrms.biz.role.model.dto.RoleDTO;
import com.example.hrms.biz.role.service.RoleService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name = "Role API v1")
@RestController
@RequestMapping("/api/v1/roles")
public class RoleRestController {

    private final RoleService roleService;

    public RoleRestController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "List roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get success",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RoleDTO.Resp.class)))})
    })
    @GetMapping("")
    public ResultPageData<List<RoleDTO.Resp>> list(RoleCriteria criteria) {
        int total = roleService.count(criteria);
        ResultPageData<List<RoleDTO.Resp>> response = new ResultPageData<>(criteria, total);
        if (total > 0) {
            response.setResultData(roleService.list(criteria));
        } else {
            response.setResultData(Collections.emptyList());
        }
        return response;
    }

    @Operation(summary = "List all roles without filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get success",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Role.class)))})
    })
    @GetMapping("/all")
    public ResultData<List<Role>> listAll() {
        List<Role> roles = roleService.getAllRoles();
        return new ResultData<>(Result.SUCCESS, "Roles retrieved successfully", roles);
    }
    @Operation(summary = "Create role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class))})
    })
    @PostMapping("")
    public Result createRole(@RequestBody RoleDTO.Req roleReq) {
        roleService.insert(roleReq);
        return new Result("Success", "Role created successfully.");
    }

    @Operation(summary = "Update role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class))})
    })
    @PutMapping("/{id}")
    public Result updateRole(@PathVariable Long id, @RequestBody RoleDTO.Req roleReq) {
        Role role = roleReq.toRole();
        role.setRoleId(id);
        roleService.updateRole(role);
        return new Result("Success", "Role updated successfully.");
    }

    @Operation(summary = "Delete role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role deleted",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class))})
    })
    @DeleteMapping("/{id}")
    public Result deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return new Result("Success", "Role deleted successfully.");
    }
}