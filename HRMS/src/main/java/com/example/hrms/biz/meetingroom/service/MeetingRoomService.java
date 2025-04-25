package com.example.hrms.biz.meetingroom.service;

import com.example.hrms.biz.meetingroom.model.MeetingRoom;
import com.example.hrms.biz.meetingroom.model.criteria.MeetingRoomCriteria;
import com.example.hrms.biz.meetingroom.model.dto.MeetingRoomDTO;
import com.example.hrms.biz.meetingroom.repository.MeetingRoomMapper;
import com.example.hrms.common.http.criteria.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MeetingRoomService {
    private final MeetingRoomMapper mapper;

    public MeetingRoomService(MeetingRoomMapper mapper) {
        this.mapper = mapper;
    }

    public int count(MeetingRoomCriteria criteria) {
        log.info("Counting meeting rooms with criteria: {}", criteria);
        return mapper.count(criteria);
    }
    public List<MeetingRoomDTO.Resp> list(Page page, MeetingRoomCriteria mCriteria) {
        page.setPageSize(100);
        page.validate();
        log.info("Fetching meeting room list with criteria: {}", mCriteria);
        try {
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
            log.info("Number of meeting rooms fetched: {}", meetings.size());
            return meetings.stream().map(MeetingRoomDTO.Resp::toResponse).toList();
        } catch (Exception e) {
            log.error("Error fetching meeting room list", e);
            throw new RuntimeException("Could not fetch meeting room list, please try again later.");
        }
    }
}
