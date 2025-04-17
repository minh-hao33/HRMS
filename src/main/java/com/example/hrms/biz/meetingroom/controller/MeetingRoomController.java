package com.example.hrms.biz.meetingroom.controller;

import com.example.hrms.biz.meetingroom.model.MeetingRoom;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/meeting-room")
public class MeetingRoomController {
    @RequestMapping("")
    public String openMeetingView(Model model) {
        model.addAttribute("meeting-room", new MeetingRoom());
        return "meeting-room"; }
}