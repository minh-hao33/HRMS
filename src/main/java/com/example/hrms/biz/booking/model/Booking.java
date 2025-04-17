package com.example.hrms.biz.booking.model;

import com.example.hrms.enumation.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private Long bookingId;
    private String username;
    private Long roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatusEnum status;
    private String title;
    private String attendees;
    private String content;
    private BookingType bookingType;
    private String weekdays;
}