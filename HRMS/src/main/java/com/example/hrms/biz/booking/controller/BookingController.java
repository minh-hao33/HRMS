package com.example.hrms.biz.booking.controller;

import com.example.hrms.biz.booking.model.Booking;
import com.example.hrms.biz.booking.model.dto.BookingDTO;
import com.example.hrms.biz.booking.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("")
    public String openBookingView(Model model) {
        model.addAttribute("booking", new BookingDTO.Req());
        return "bookings";
    }

    @GetMapping("/view")
    public String openViewRoom(Model model) {
        List<BookingDTO.Resp> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "viewroom";
    }

    @GetMapping("/edit/{bookingId}")
    public String openEditBookingView(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.getBookingById(bookingId);
        model.addAttribute("booking", BookingDTO.Req.fromBooking(booking));
        return "bookingedit";
    }

    @GetMapping("/delete/{bookingId}")
    public String openDeletePopup(@PathVariable Long bookingId, Model model) {
        model.addAttribute("bookingId", bookingId);
        return "popup";
    }

    @PostMapping("/delete/{bookingId}")
    public String deleteBooking(@PathVariable Long bookingId) {
        bookingService.deleteBooking(bookingId);
        return "redirect:/bookings/view";
    }
}
