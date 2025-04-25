package com.example.hrms.biz.booking.model.criteria;

import com.example.hrms.common.http.criteria.Page;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingCriteria extends Page {
    private String username;
    private Long roomId;
    private String status;
    private String title;
    private String attendees;
    private String content;
    private String bookingType;
    private String weekdays;
}