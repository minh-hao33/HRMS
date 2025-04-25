package com.example.hrms.biz.meetingroom.controller;

import com.example.hrms.biz.meetingroom.controller.rest.MeetingRoomRestController;
import com.example.hrms.biz.meetingroom.model.criteria.MeetingRoomCriteria;
import com.example.hrms.biz.meetingroom.model.dto.MeetingRoomDTO;
import com.example.hrms.biz.meetingroom.service.MeetingRoomService;
import com.example.hrms.common.http.criteria.Page;
import com.example.hrms.common.http.model.ResultPageData;
import com.example.hrms.utils.MeetingRoomUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MeetingRoomRestControllerTest {

    @Mock
    private MeetingRoomService service;

    @InjectMocks
    private MeetingRoomRestController controller;

    public MeetingRoomRestControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListWithResults() {
        // Initialize page and criteria
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(2);

        MeetingRoomCriteria criteria = new MeetingRoomCriteria();

        // Mock data using the builder pattern

        List<MeetingRoomDTO.Resp> mockData = List.of(
                new MeetingRoomUtils.Builder()
                        .roomId(1L)
                        .roomName("Room A")
                        .location("Floor 1")
                        .capacity(10)
                        .username("User A")
                        .startTime(LocalDateTime.of(2025, 3, 1, 10, 0))
                        .endTime(LocalDateTime.of(2025, 3, 1, 12, 0))
                        .status(null)
                        .buildResponse(), // Correct usage for Resp

                new MeetingRoomUtils.Builder()
                        .roomId(2L)
                        .roomName("Room B")
                        .location("Floor 2")
                        .capacity(15)
                        .username("User B")
                        .startTime(LocalDateTime.of(2025, 3, 2, 14, 0))
                        .endTime(LocalDateTime.of(2025, 3, 2, 16, 0))
                        .status(null)
                        .buildResponse() // Correct usage for Resp
        );

        // Mock service responses
        when(service.count(criteria)).thenReturn(2);
        when(service.list(page, criteria)).thenReturn(mockData);

        // Execute controller method
        ResultPageData<List<MeetingRoomDTO.Resp>> response = controller.list(page, criteria);

        // Assertions
        assertEquals(2, response.getResultData().size(), "Expected 2 meeting rooms in response.");
        assertEquals("Room A", response.getResultData().get(0).getRoomName());
        assertEquals("Room B", response.getResultData().get(1).getRoomName());
    }

    @Test
    void testListWithoutResults() {
        // Initialize page and criteria
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(10);

        MeetingRoomCriteria criteria = new MeetingRoomCriteria();

        // Mock service response for no results
        when(service.count(criteria)).thenReturn(0);

        // Execute controller method
        ResultPageData<List<MeetingRoomDTO.Resp>> response = controller.list(page, criteria);

        // Assertions
        assertEquals(0, response.getResultData().size(), "Expected no meeting rooms in response.");
    }

    @Test
    void testListWithInvalidPagination() {
        // Initialize invalid pagination values
        Page page = new Page();
        page.setPageNo(0); // Invalid page number
        page.setPageSize(0); // Invalid page size

        MeetingRoomCriteria criteria = new MeetingRoomCriteria();

        // Mock service response for no results
        when(service.count(criteria)).thenReturn(0);

        // Execute controller method
        ResultPageData<List<MeetingRoomDTO.Resp>> response = controller.list(page, criteria);

        // Assertions
        assertEquals(0, response.getResultData().size(), "Expected no meeting rooms in response with invalid pagination.");
    }

    @Test
    void testListWithServiceException() {
        // Initialize page and criteria
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(10);

        MeetingRoomCriteria criteria = new MeetingRoomCriteria();

        // Mock service exception
        when(service.count(criteria)).thenThrow(new RuntimeException("Service exception"));

        // Execute controller method and assert exception
        Exception exception = assertThrows(RuntimeException.class, () -> controller.list(page, criteria));
        assertEquals("Service exception", exception.getMessage(), "Expected exception message to match.");
    }

    @Test
    void testListWithNullCriteria() {
        // Initialize page and null criteria
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(10);

        // Mock service response for null criteria
        when(service.count(null)).thenReturn(0);

        // Execute controller method
        ResultPageData<List<MeetingRoomDTO.Resp>> response = controller.list(page, null);

        // Assertions
        assertEquals(0, response.getResultData().size(), "Expected no meeting rooms in response for null criteria.");
    }
}
