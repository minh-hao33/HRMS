package com.example.hrms.biz.meetingroom.model.criteria;


import com.example.hrms.common.http.criteria.Page;
import com.example.hrms.enumation.BookingStatusEnum;
import com.example.hrms.enumation.BookingType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MeetingRoomCriteria extends Page {
    private Long roomId;
    private String roomName;
    private String location;
    private Integer capacity;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatusEnum status;
    private String title;
    private String attendees;
    private String content;
    private BookingType bookingType;
    private String weekdays;
}
