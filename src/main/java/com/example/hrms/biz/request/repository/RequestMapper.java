package com.example.hrms.biz.request.repository;

import com.example.hrms.biz.request.model.Request;
import com.example.hrms.biz.request.model.criteria.RequestCriteria;
import com.example.hrms.enumation.RequestStatusEnum;
import com.example.hrms.enumation.RequestTypeEnum;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface RequestMapper {

    @Select("SELECT COUNT(request_id) " +
            "FROM requests r " +
            "WHERE " +
            "    (#{requestId} IS NULL OR r.request_id = #{requestId}) " +
            "    AND (#{username} IS NULL OR r.username = #{username}) " +
            "    AND (#{departmentId} IS NULL OR r.department_id = #{departmentId}) " +
            "    AND (#{requestType} IS NULL OR r.request_type = #{requestType}) " +
            "    AND (#{requestReason} IS NULL OR r.request_reason = #{requestReason}) " +
            "    AND (#{requestStatus} IS NULL OR r.request_status = #{requestStatus}) " +
            "    AND (#{approverUsername} IS NULL OR r.approver_username = #{approverUsername}) " +
            "    AND (#{startTime} IS NULL OR r.start_time >= #{startTime}) " +
            "    AND (#{endTime} IS NULL OR r.end_time <= #{endTime})")
    int count(RequestCriteria criteria);

    @Select("SELECT " +
            "    r.request_id AS requestId, " +
            "    r.username AS username, " +
            "    r.department_id AS departmentId, " +
            "    r.request_type AS requestType, " +
            "    r.request_reason AS requestReason, " +
            "    r.request_status AS requestStatus, " +
            "    r.approver_username AS approverUsername, " +
            "    r.start_time AS startTime, " +
            "    r.end_time AS endTime, " +
            "    r.created_at AS createdAt, " +
            "    r.updated_at AS updatedAt, " +
            "    r.approved_at AS approvedAt, " +
            "    r.rejection_reason AS rejectionReason " +  // Thêm rejectionReason
            "FROM requests r " +
            "WHERE " +
            "    (#{requestId} IS NULL OR r.request_id = #{requestId}) " +
            "    AND (#{username} IS NULL OR r.username = #{username}) " +
            "    AND (#{departmentId} IS NULL OR r.department_id = #{departmentId}) " +
            "    AND (#{requestType} IS NULL OR r.request_type = #{requestType}) " +
            "    AND (#{requestReason} IS NULL OR r.request_reason = #{requestReason}) " +
            "    AND (#{requestStatus} IS NULL OR r.request_status = #{requestStatus}) " +
            "    AND (#{approverUsername} IS NULL OR r.approver_username = #{approverUsername}) " +
            "    AND (#{startTime} IS NULL OR r.start_time >= #{startTime}) " +
            "    AND (#{endTime} IS NULL OR r.end_time <= #{endTime}) " +
            "ORDER BY r.request_id ASC " +
            "LIMIT #{pageSize} OFFSET #{offset}")
    List<Request> select(@Param("pageSize") int pageSize,
                         @Param("offset") int offset,
                         @Param("requestId") Long requestId,
                         @Param("username") String username,
                         @Param("departmentId") Long departmentId,
                         @Param("requestType") RequestTypeEnum requestType,
                         @Param("requestReason") String requestReason,
                         @Param("requestStatus") RequestStatusEnum requestStatus,
                         @Param("approverUsername") String approverUsername,
                         @Param("startTime") Date startTime,
                         @Param("endTime") Date endTime);


    @Select("SELECT " +
            "    r.request_id AS requestId, " +
            "    r.username AS username, " +
            "    r.department_id AS departmentId, " +
            "    r.request_type AS requestType, " +
            "    r.request_reason AS requestReason, " +
            "    r.request_status AS requestStatus, " +
            "    r.approver_username AS approverUsername, " +
            "    r.start_time AS startTime, " +
            "    r.end_time AS EndTime, " +
            "    r.created_at AS createdAt, " +
            "    r.updated_at AS updatedAt, " +
            "    r.approved_at AS approvedAt, " +
            "    r.rejection_reason AS rejectionReason " + // Thêm rejectionReason
            "FROM requests r " +
            "WHERE r.department_id = (SELECT department_id FROM users WHERE username = #{username})")

    List<Request> getRequestsBySupervisor(@Param("username") String username);

    @Update("UPDATE requests " +
            "SET request_status = #{status}, approver_username = #{approverUsername}, rejection_reason = #{rejectionReason}, " +
            "approved_at = CURRENT_TIMESTAMP " +
            "WHERE request_id = #{requestId}")
    int updateRequestStatus(@Param("requestId") Long requestId,
                            @Param("status") RequestStatusEnum status,
                            @Param("approverUsername") String approverUsername,
                            @Param("rejectionReason") String rejectionReason);

  @Select("SELECT COALESCE(SUM(DATEDIFF(end_time, start_time) + 1), 0) " +
      "FROM requests " +
      "WHERE request_status = 'APPROVED' " +
      "AND request_type IN ('PAID_LEAVE', 'EMPLOYEE_WEDDING_LEAVE', 'CHILD_WEDDING_LEAVE', 'FAMILY_DEATH_LEAVE', 'SICK_LEAVE', 'MATERNITY_LEAVE', 'PATERNITY_LEAVE', 'COMPENSATORY_LEAVE') " +
      "AND username = #{username}")
  Integer calculateTotalLeaveDays(@Param("username") String username);
    @Insert("INSERT INTO Requests (username, department_id, request_type, request_reason, request_status, approver_username, start_time, end_time) " +
            "VALUES (#{username}, #{departmentId}, #{requestType}, #{requestReason}, COALESCE(#{requestStatus}, 'PENDING'), #{approverUsername}, #{startTime}, #{endTime})")
    @Options(useGeneratedKeys = true, keyProperty = "requestId")
    void insertRequest(Request request);

    @Select("SELECT department_id FROM Users WHERE username = #{username}")
    Long findDepartmentByUsername(@Param("username") String username);

    @Select("SELECT approver_username FROM Requests WHERE department_id = #{departmentId} " +
            "ORDER BY created_at DESC LIMIT 1")
    String findLatestApproverByDepartment(@Param("departmentId") Long departmentId);
    @Update("UPDATE Requests SET request_type = #{requestType}, request_reason = #{requestReason}, " +
            "start_time = #{startTime}, end_time = #{endTime}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE request_id = #{requestId} AND request_status = 'PENDING'")
    int updateRequest(Request request);
    @Select("SELECT * FROM requests WHERE request_id = #{requestId}")
    Request findById(@Param("requestId") Long requestId);

    @Delete("DELETE FROM requests WHERE request_id = #{requestId} AND request_status NOT IN ('REJECTED', 'APPROVED')")
    int deleteRequest(@Param("requestId") Long requestId);

}