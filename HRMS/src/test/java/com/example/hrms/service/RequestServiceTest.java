//package com.example.hrms.service;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.example.hrms.biz.request.service.RequestService;
//import com.example.hrms.biz.request.model.Request;
//import com.example.hrms.biz.request.repository.RequestMapper;
//import com.example.hrms.enumation.RequestStatusEnum;
//import com.example.hrms.enumation.RequestTypeEnum;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//public class RequestServiceTest {
//
//    @Mock
//    private RequestMapper requestMapper;
//
//    @InjectMocks
//    private RequestService requestService;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testGetRequestById() {
//        Long requestId = 1L;
//        Request request = new Request();
//        request.setRequestId(requestId);
//        request.setUsername("Trung Du Nguyen");
//        request.setDepartmentId(1L);
//        request.setRequestType(RequestTypeEnum.PAID_LEAVE);
//        request.setRequestReason("Nghỉ phép cá nhân");
//        request.setRequestStatus(RequestStatusEnum.REJECTED);
//        request.setApproverUsername("Minh Hao Pham");
//        request.setStartTime(java.sql.Timestamp.valueOf("2025-02-28 09:00:00"));
//        request.setEndTime(java.sql.Timestamp.valueOf("2025-02-28 17:00:00"));
//
//        when(requestMapper.getRequestById(requestId)).thenReturn(request);
//
//        Request result = requestService.getRequestById(requestId);
//
//        assertEquals(request, result);
//        verify(requestMapper).getRequestById(requestId);
//    }
//
//    @Test
//    public void testGetRequestByIdNonExistent() {
//        Long requestId = 999L;
//
//        when(requestMapper.getRequestById(requestId)).thenReturn(null);
//
//        Request result = requestService.getRequestById(requestId);
//
//        assertNull(result);
//        verify(requestMapper).getRequestById(requestId);
//    }
//
//    @Test
//    public void testInsertRequest() {
//        Request request = new Request();
//        request.setUsername("Minh Hao Pham");
//        request.setDepartmentId(1L);
//        request.setRequestType(RequestTypeEnum.PAID_LEAVE);
//        request.setRequestReason("Nghỉ phép đột xuất");
//        request.setRequestStatus(RequestStatusEnum.APPROVED);
//        request.setApproverUsername("Khac Khanh Bui");
//        request.setStartTime(java.sql.Timestamp.valueOf("2025-02-27 09:00:00"));
//        request.setEndTime(java.sql.Timestamp.valueOf("2025-02-27 17:00:00"));
//
//        requestService.insertRequest(request);
//
//        verify(requestMapper).insertRequest(request);
//    }
//
//    @Test
//    public void testInsertRequestNull() {
//        Request request = null;
//
//        requestService.insertRequest(request);
//
//        verify(requestMapper, never()).insertRequest(request);
//    }
//
//    @Test
//    public void testInsertRequestMissingFields() {
//        Request request = new Request();
//        // Missing required fields like username, departmentId, etc.
//
//        requestService.insertRequest(request);
//
//        verify(requestMapper, never()).insertRequest(request);
//    }
//
//    @Test
//    public void testUpdateRequest() {
//        Request request = new Request();
//        request.setRequestId(3L);
//        request.setUsername("Trung Du Nguyen");
//        request.setDepartmentId(1L);
//        request.setRequestType(RequestTypeEnum.UNPAID_LEAVE);
//        request.setRequestReason("Làm việc từ xa do bệnh");
//        request.setRequestStatus(RequestStatusEnum.REJECTED);
//        request.setApproverUsername("Minh Hao Pham");
//        request.setStartTime(java.sql.Timestamp.valueOf("2025-02-26 09:00:00"));
//        request.setEndTime(java.sql.Timestamp.valueOf("2025-02-26 17:00:00"));
//
//        when(requestMapper.getRequestById(request.getRequestId())).thenReturn(request);
//
//        requestService.updateRequest(request);
//
//        verify(requestMapper).updateRequest(request);
//    }
//
//    @Test
//    public void testUpdateRequestNonExistent() {
//        Request request = new Request();
//        request.setRequestId(999L);
//
//        when(requestMapper.getRequestById(request.getRequestId())).thenReturn(null);
//
//        requestService.updateRequest(request);
//
//        verify(requestMapper, never()).updateRequest(request);
//    }
//
//    @Test
//    public void testUpdateRequestNull() {
//        Request request = null;
//
//        requestService.updateRequest(request);
//
//        verify(requestMapper, never()).updateRequest(request);
//    }
//
//    @Test
//    public void testDeleteRequest() {
//        Long requestId = 4L;
//        Request request = new Request();
//        request.setRequestId(requestId);
//
//        when(requestMapper.getRequestById(requestId)).thenReturn(request);
//
//        requestService.deleteRequest(requestId);
//
//        verify(requestMapper).deleteRequest(requestId);
//    }
//
//    @Test
//    public void testDeleteRequestNonExistent() {
//        Long requestId = 999L;
//
//        when(requestMapper.getRequestById(requestId)).thenReturn(null);
//
//        requestService.deleteRequest(requestId);
//
//        verify(requestMapper, never()).deleteRequest(requestId);
//    }
//
//    @Test
//    public void testDeleteRequestNullId() {
//        Long requestId = null;
//
//        requestService.deleteRequest(requestId);
//
//        verify(requestMapper, never()).deleteRequest(requestId);
//    }
//}