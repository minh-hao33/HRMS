package com.example.hrms.biz.meetingroom.model;

import com.example.hrms.enumation.BookingStatusEnum;
import com.example.hrms.enumation.BookingType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoom {
    private Long roomId;
    private String roomName;
    private String location;
    private Integer capacity;

    // Booking fields (from the JOIN query)
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
