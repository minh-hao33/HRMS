package com.example.hrms.utils;

import com.example.hrms.biz.meetingroom.model.MeetingRoom;
import com.example.hrms.biz.meetingroom.model.dto.MeetingRoomDTO;
import com.example.hrms.enumation.BookingStatusEnum;
import com.example.hrms.enumation.BookingType;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

public class MeetingRoomUtils {

    // Convert DTO Request to MeetingRoom
    public static MeetingRoom toMeetingRoom(MeetingRoomDTO.Req req) {
        if (req == null) {
            return null;
        }
        MeetingRoom meetingRoom = new MeetingRoom();
        BeanUtils.copyProperties(req, meetingRoom);
        return meetingRoom;
    }

    // Convert MeetingRoom to DTO Response
    public static MeetingRoomDTO.Resp toResponse(MeetingRoom meetingRoom) {
        if (meetingRoom == null) {
            return null;
        }
        MeetingRoomDTO.Resp resp = new MeetingRoomDTO.Resp();
        resp.setRoomId(meetingRoom.getRoomId());
        resp.setRoomName(meetingRoom.getRoomName());
        resp.setLocation(meetingRoom.getLocation());
        resp.setCapacity(meetingRoom.getCapacity());
        resp.setUsername(meetingRoom.getUsername());
        resp.setStartTime(meetingRoom.getStartTime());
        resp.setEndTime(meetingRoom.getEndTime());
        resp.setStatus(meetingRoom.getStatus());
        resp.setTitle(meetingRoom.getTitle());
        resp.setAttendees(meetingRoom.getAttendees());
        resp.setContent(meetingRoom.getContent());
        resp.setBookingType(meetingRoom.getBookingType());
        resp.setWeekdays(meetingRoom.getWeekdays());
        return resp;
    }

    // Flexible Builder for MeetingRoom and Resp
    public static class Builder {
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

        public Builder roomId(Long roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder roomName(String roomName) {
            this.roomName = roomName;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder capacity(Integer capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder status(BookingStatusEnum status) {
            this.status = status;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder attendees(String attendees) {
            this.attendees = attendees;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder bookingType(BookingType bookingType) {
            this.bookingType = bookingType;
            return this;
        }

        public Builder weekdays(String weekdays) {
            this.weekdays = weekdays;
            return this;
        }

        public MeetingRoom buildMeetingRoom() {
            MeetingRoom meetingRoom = new MeetingRoom();
            meetingRoom.setRoomId(this.roomId);
            meetingRoom.setRoomName(this.roomName);
            meetingRoom.setLocation(this.location);
            meetingRoom.setCapacity(this.capacity);
            meetingRoom.setUsername(this.username);
            meetingRoom.setStartTime(this.startTime);
            meetingRoom.setEndTime(this.endTime);
            meetingRoom.setStatus(this.status);
            meetingRoom.setTitle(this.title);
            meetingRoom.setAttendees(this.attendees);
            meetingRoom.setContent(this.content);
            meetingRoom.setBookingType(this.bookingType);
            meetingRoom.setWeekdays(this.weekdays);
            return meetingRoom;
        }

        public MeetingRoomDTO.Resp buildResponse() {
            MeetingRoomDTO.Resp resp = new MeetingRoomDTO.Resp();
            resp.setRoomId(this.roomId);
            resp.setRoomName(this.roomName);
            resp.setLocation(this.location);
            resp.setCapacity(this.capacity);
            resp.setUsername(this.username);
            resp.setStartTime(this.startTime);
            resp.setEndTime(this.endTime);
            resp.setStatus(this.status);
            resp.setTitle(this.title);
            resp.setAttendees(this.attendees);
            resp.setContent(this.content);
            resp.setBookingType(this.bookingType);
            resp.setWeekdays(this.weekdays);
            return resp;
        }
    }
}