package com.example.hrms.biz.booking.model.dto;

import com.example.hrms.biz.booking.model.Booking;
import com.example.hrms.enumation.BookingType;
import com.example.hrms.enumation.BookingStatusEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import com.example.hrms.utils.DateUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookingDTO {

    @Getter
    @Setter
    public static class Req {
        private Long bookingId;
        private String username;
        private Long roomId;
        private String title;
        private String attendees;
        private String content;
        private String startTime;
        private String endTime;
        private String status;
        private String bookingType;
        private String weekdays;

        public static Req fromBooking(Booking booking) {
            Req req = new Req();
            BeanUtils.copyProperties(booking, req);
            DateTimeFormatter formatter = DateUtils.getDateTimeFormatter();
            req.setStartTime(booking.getStartTime() != null ? booking.getStartTime().format(formatter) : null);
            req.setEndTime(booking.getEndTime() != null ? booking.getEndTime().format(formatter) : null);
            req.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
            req.setTitle(booking.getTitle());
            req.setAttendees(booking.getAttendees());
            req.setContent(booking.getContent());
            req.setBookingType(booking.getBookingType() != null ? booking.getBookingType().name() : null);
            req.setWeekdays(booking.getWeekdays());
            return req;
        }

        public Booking toBooking() {
            Booking booking = new Booking();
            BeanUtils.copyProperties(this, booking);

            DateTimeFormatter formatter = DateUtils.getDateTimeFormatter(); // dùng formatter có giây
            booking.setStartTime(this.startTime != null ? LocalDateTime.parse(this.startTime, formatter) : null);
            booking.setEndTime(this.endTime != null ? LocalDateTime.parse(this.endTime, formatter) : null);

            booking.setStatus(this.status != null ? BookingStatusEnum.valueOf(this.status) : BookingStatusEnum.Confirmed);
            booking.setBookingType(this.bookingType != null ? BookingType.valueOf(this.bookingType) : BookingType.ONLY);
            booking.setWeekdays(this.weekdays);
            booking.setUsername(this.username);
            booking.setRoomId(this.roomId != null ? this.roomId : 1L);
            booking.setContent(this.content);

            return booking;
        }
    }

    @Getter
    @Setter
    public static class Resp {
        private Long bookingId;
        private String username;
        private Long roomId;
        private String title;
        private String attendees;
        private String content;
        private String startTime;
        private String endTime;
        private String status;
        private String bookingType;
        private String weekdays;

        public static Resp toResponse(Booking booking) {
            Resp resp = new Resp();
            BeanUtils.copyProperties(booking, resp);
            DateTimeFormatter formatter = DateUtils.getDateTimeFormatter();
            resp.setStartTime(booking.getStartTime() != null ? booking.getStartTime().format(formatter) : null);
            resp.setEndTime(booking.getEndTime() != null ? booking.getEndTime().format(formatter) : null);
            resp.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
            resp.setTitle(booking.getTitle());
            resp.setAttendees(booking.getAttendees());
            resp.setContent(booking.getContent());
            resp.setBookingType(booking.getBookingType() != null ? booking.getBookingType().name() : null);
            resp.setWeekdays(booking.getWeekdays());
            return resp;
        }
    }

    @Getter
    @Setter
    public static class CheckReq {
        private String startTime;
        private String endTime;
        public Booking toBooking() {
            Booking booking = new Booking();
            DateTimeFormatter formatter = DateUtils.getDateTimeFormatter();
            booking.setStartTime(LocalDateTime.parse(this.startTime, formatter));
            booking.setEndTime(LocalDateTime.parse(this.endTime, formatter));
            return booking;
        }
    }
}
