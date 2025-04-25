package com.example.hrms.biz.meetingroom.service;

import com.example.hrms.biz.meetingroom.model.MeetingRoom;
import com.example.hrms.biz.meetingroom.model.criteria.MeetingRoomCriteria;
import com.example.hrms.biz.meetingroom.model.dto.MeetingRoomDTO;
import com.example.hrms.biz.meetingroom.repository.MeetingRoomMapper;
import com.example.hrms.common.http.criteria.Page;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MeetingRoomServiceTest {

  @Mock
  private MeetingRoomMapper mapper;

  @InjectMocks
  private MeetingRoomService service;

  public MeetingRoomServiceTest() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCount() {
    MeetingRoomCriteria criteria = new MeetingRoomCriteria();
    when(mapper.count(criteria)).thenReturn(5);

    int total = service.count(criteria);
    assertEquals(5, total, "Expected count of meeting rooms to be 5.");
    verify(mapper, times(1)).count(criteria);
  }

  @Test
  void testList() {
    Page page = new Page();
    page.setPageNo(1);
    page.setPageSize(2);

    MeetingRoomCriteria criteria = new MeetingRoomCriteria();

    // Mocking data
    List<MeetingRoom> mockRooms = new ArrayList<>();
    mockRooms.add(new MeetingRoom(
        1L, "Room A", "Floor 1", 10,
        "User A", LocalDateTime.of(2025, 3, 1, 10, 0),
        LocalDateTime.of(2025, 3, 1, 12, 0),
        null, "Title A", "Attendees A",
        "Content A", null, null
    ));
    mockRooms.add(new MeetingRoom(
        2L, "Room B", "Floor 2", 15,
        "User B", LocalDateTime.of(2025, 3, 2, 14, 0),
        LocalDateTime.of(2025, 3, 2, 16, 0),
        null, "Title B", "Attendees B",
        "Content B", null, null
    ));

    when(mapper.select(page.getPageSize(), page.getOffset(),
        criteria.getRoomId(), criteria.getRoomName(), criteria.getLocation(),
        criteria.getCapacity(), criteria.getUsername(),
        criteria.getStartTime(), criteria.getEndTime(),
        criteria.getStatus(), criteria.getTitle(),
        criteria.getAttendees(), criteria.getContent(),
        criteria.getBookingType(), criteria.getWeekdays())).thenReturn(mockRooms);

    // Execute test
    List<MeetingRoomDTO.Resp> result = service.list(page, criteria);

    // Assertions
    assertEquals(2, result.size(), "Expected 2 meeting rooms in result.");
    assertEquals("Room A", result.get(0).getRoomName());
    assertEquals("Room B", result.get(1).getRoomName());
    verify(mapper, times(1)).select(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),any());
  }

  @Test
  void testListWithException() {
    Page page = new Page();
    page.setPageNo(1);
    page.setPageSize(2);

    MeetingRoomCriteria criteria = new MeetingRoomCriteria();
    when(mapper.select(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),any()))
        .thenThrow(new RuntimeException("Database error"));

    // Execute and assert exception
    Exception exception = assertThrows(RuntimeException.class, () -> service.list(page, criteria));
    assertEquals("Unable to fetch meeting rooms. Please try again.", exception.getMessage());
    verify(mapper, times(1)).select(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),any());
  }
}