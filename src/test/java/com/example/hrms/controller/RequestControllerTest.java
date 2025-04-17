//package com.example.hrms.controller;
//
//import com.example.hrms.biz.request.model.Request;
//import com.example.hrms.biz.request.service.RequestService;
//import com.example.hrms.biz.request.controller.RequestController;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(MockitoExtension.class)
//public class RequestControllerTest {
//
//    @Mock
//    private RequestService requestService;
//
//    @InjectMocks
//    private RequestController requestController;
//
//    private MockMvc mockMvc;
//
//    @Test
//    public void testGetRequestById() throws Exception {
//        Request request = new Request();
//        request.setRequestId(1L);
//        when(requestService.getRequestById(1L)).thenReturn(request);
//
//        mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
//        mockMvc.perform(get("/requests/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.requestId").value(1L));
//
//        verify(requestService, times(1)).getRequestById(1L);
//    }
//
//    @Test
//    public void testInsertRequest() throws Exception {
//        Request request = new Request();
//        doNothing().when(requestService).insertRequest(any(Request.class));
//
//        mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
//        mockMvc.perform(post("/requests")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"username\":\"testuser\",\"departmentId\":1,\"requestType\":\"PAID_LEAVE\",\"requestReason\":\"Nghỉ phép cá nhân\",\"requestStatus\":\"APPROVED\",\"approverUsername\":\"approver\",\"startTime\":\"2025-02-28T09:00:00\",\"endTime\":\"2025-02-28T17:00:00\"}"))
//                .andExpect(status().isOk());
//
//        verify(requestService, times(1)).insertRequest(any(Request.class));
//    }
//
//    @Test
//    public void testUpdateRequest() throws Exception {
//        Request request = new Request();
//        request.setRequestId(1L);
//        doNothing().when(requestService).updateRequest(any(Request.class));
//
//        mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
//        mockMvc.perform(put("/requests/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"username\":\"testuser\",\"departmentId\":1,\"requestType\":\"PAID_LEAVE\",\"requestReason\":\"Nghỉ phép cá nhân\",\"requestStatus\":\"APPROVED\",\"approverUsername\":\"approver\",\"startTime\":\"2025-02-28T09:00:00\",\"endTime\":\"2025-02-28T17:00:00\"}"))
//                .andExpect(status().isOk());
//
//        verify(requestService, times(1)).updateRequest(any(Request.class));
//    }
//
//    @Test
//    public void testDeleteRequest() throws Exception {
//        doNothing().when(requestService).deleteRequest(1L);
//
//        mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
//        mockMvc.perform(delete("/requests/1"))
//                .andExpect(status().isOk());
//
//        verify(requestService, times(1)).deleteRequest(1L);
//    }
//}
//
