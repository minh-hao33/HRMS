package com.example.hrms.biz.meetingroom.repository;

import com.example.hrms.biz.meetingroom.model.MeetingRoom;
import com.example.hrms.biz.meetingroom.model.criteria.MeetingRoomCriteria;
import com.example.hrms.common.http.criteria.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class MeetingRoomMapperTest {

    @Autowired
    private MeetingRoomMapper mapper;

    @Test
    public void testPagination() {
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(2);

        MeetingRoomCriteria mCriteria = new MeetingRoomCriteria();

        List<MeetingRoom> meetings = mapper.select(
            page.getPageSize(),
            page.getOffset(),
            mCriteria.getRoomId(),
            mCriteria.getRoomName(),
            mCriteria.getLocation(),
            mCriteria.getCapacity(),
            mCriteria.getUsername(),
            mCriteria.getStartTime(),
            mCriteria.getEndTime(),
            mCriteria.getStatus(),
            mCriteria.getTitle(),
            mCriteria.getAttendees(),
            mCriteria.getContent(),
            mCriteria.getBookingType(),
            mCriteria.getWeekdays()
        );

        assertEquals(2, meetings.size(), "Expected 2 rooms for page 1.");
    }

    @Test
    public void testQueryByUsernameAndTimeRange() {
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(10);

        MeetingRoomCriteria mCriteria = new MeetingRoomCriteria();
        mCriteria.setUsername("john.doe");
        mCriteria.setStartTime(LocalDateTime.of(2025, 3, 1, 8, 0));
        mCriteria.setEndTime(LocalDateTime.of(2025, 3, 1, 12, 0));

        List<MeetingRoom> meetings = mapper.select(
            page.getPageSize(),
            page.getOffset(),
            mCriteria.getRoomId(),
            mCriteria.getRoomName(),
            mCriteria.getLocation(),
            mCriteria.getCapacity(),
            mCriteria.getUsername(),
            mCriteria.getStartTime(),
            mCriteria.getEndTime(),
            mCriteria.getStatus(),
            mCriteria.getTitle(),
            mCriteria.getAttendees(),
            mCriteria.getContent(),
            mCriteria.getBookingType(),
            mCriteria.getWeekdays()
        );

        assertTrue(meetings.size() > 0, "Expected rooms for user 'john.doe' within the specified time range.");
    }

    @Test
    public void testQueryWithNullCriteria() {
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(10);

        MeetingRoomCriteria mCriteria = new MeetingRoomCriteria();

        List<MeetingRoom> meetings = mapper.select(
            page.getPageSize(),
            page.getOffset(),
            mCriteria.getRoomId(),
            mCriteria.getRoomName(),
            mCriteria.getLocation(),
            mCriteria.getCapacity(),
            mCriteria.getUsername(),
            mCriteria.getStartTime(),
            mCriteria.getEndTime(),
            mCriteria.getStatus(),
            mCriteria.getTitle(),
            mCriteria.getAttendees(),
            mCriteria.getContent(),
            mCriteria.getBookingType(),
            mCriteria.getWeekdays()
        );

        assertTrue(meetings.size() > 0, "Expected all rooms when criteria is null.");
    }

    @Test
    public void testNoMatchingResults() {
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(10);

        MeetingRoomCriteria mCriteria = new MeetingRoomCriteria();
        mCriteria.setUsername("nonexistent.user");
        mCriteria.setStartTime(LocalDateTime.of(2025, 3, 1, 8, 0));
        mCriteria.setEndTime(LocalDateTime.of(2025, 3, 1, 12, 0));

        List<MeetingRoom> meetings = mapper.select(
            page.getPageSize(),
            page.getOffset(),
            mCriteria.getRoomId(),
            mCriteria.getRoomName(),
            mCriteria.getLocation(),
            mCriteria.getCapacity(),
            mCriteria.getUsername(),
            mCriteria.getStartTime(),
            mCriteria.getEndTime(),
            mCriteria.getStatus(),
            mCriteria.getTitle(),
            mCriteria.getAttendees(),
            mCriteria.getContent(),
            mCriteria.getBookingType(),
            mCriteria.getWeekdays()
        );

        assertTrue(meetings.isEmpty(), "Expected no matching rooms for nonexistent user.");
    }

    @Test
    public void testInvalidPaginationValues() {
        Page page = new Page();
        page.setPageNo(0); // Invalid page number
        page.setPageSize(0); // Invalid page size

        MeetingRoomCriteria mCriteria = new MeetingRoomCriteria();

        List<MeetingRoom> meetings = mapper.select(
                Math.max(page.getPageSize(), 10), // Default to a valid page size
                Math.max(page.getOffset(), 0),   // Default to a valid offset
                mCriteria.getRoomId(),
                mCriteria.getRoomName(),
                mCriteria.getLocation(),
                mCriteria.getCapacity(),
                mCriteria.getUsername(),
                mCriteria.getStartTime(),
                mCriteria.getEndTime(),
                mCriteria.getStatus(),
                mCriteria.getTitle(),
                mCriteria.getAttendees(),
                mCriteria.getContent(),
                mCriteria.getBookingType(),
                mCriteria.getWeekdays()
            );

        assertTrue(meetings.size() > 0, "Expected all rooms when invalid pagination values are corrected.");
    }
}
