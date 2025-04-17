package com.example.hrms.biz.meetingroom.model.dto;

import com.example.hrms.biz.meetingroom.model.MeetingRoom;
import com.example.hrms.enumation.BookingStatusEnum;
import com.example.hrms.enumation.BookingType;
import com.example.hrms.utils.MeetingRoomUtils;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingRoomDTO {

    public static class Req {
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

        // Convert to MeetingRoom using Utils
        public MeetingRoom toMeetingRoom() {
            return MeetingRoomUtils.toMeetingRoom(this);
        }
    }

    @Getter
    @Setter
    public static class Resp {
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

        // Convert from MeetingRoom using Utils
        public static Resp toResponse(MeetingRoom meetingRoom) {
            return MeetingRoomUtils.toResponse(meetingRoom);
        }
    }
}
