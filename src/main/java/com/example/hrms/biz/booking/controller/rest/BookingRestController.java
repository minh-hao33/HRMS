package com.example.hrms.biz.booking.controller.rest;

import com.example.hrms.biz.booking.model.Booking;
import com.example.hrms.biz.booking.model.criteria.BookingCriteria;
import com.example.hrms.biz.booking.model.dto.BookingDTO;
import com.example.hrms.biz.booking.service.BookingService;
import com.example.hrms.common.http.model.Result;
import com.example.hrms.common.http.model.ResultPageData;
import com.example.hrms.exception.InvalidArgumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jdk.jfr.Percentage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Collections;
import java.util.List;

@Tag(name = "Booking API v1")
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingRestController {

    private final BookingService bookingService;

    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "List bookings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get success",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BookingDTO.Resp.class))) }),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content) })
    @GetMapping("")
    public ResultPageData<List<BookingDTO.Resp>> list(BookingCriteria criteria) {
        int total = bookingService.count(criteria);
        ResultPageData<List<BookingDTO.Resp>> response = new ResultPageData<>(criteria, total);
        if (total > 0) {
            response.setResultData(bookingService.list(criteria));
        } else {
            response.setResultData(Collections.emptyList());
        }
        return response;
    }

    @Operation(summary = "Get booking by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get success",
                    content = @Content) })
    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @Operation(summary = "Create booking")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class)) }),
            @ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content) })
    @PostMapping("")
    public Result createBooking(@RequestBody @Valid BookingDTO.Req bookingReq) {
        Booking booking = bookingReq.toBooking();
        // Sinh danh sách các Booking dựa vào kiểu đặt phòng
        List<Booking> bookings = bookingService.handleBookingType(booking);
        // Kiểm tra xung đột cho từng booking
        for (Booking b : bookings) {
            if (bookingService.isConflict(b)) {
                throw new InvalidArgumentException("Booking time conflicts with an existing booking.");
            }
        }
        // Chèn từng booking vào DB
        for (Booking b : bookings) {
            bookingService.insert(BookingDTO.Req.fromBooking(b));
        }

        return new Result("Success", "Booking created successfully.");
    }

    @Operation(summary = "Update booking")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class)) }),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content) })
    @PutMapping("/{id}")
    public Result updateBooking(@PathVariable Long id, @RequestBody @Valid BookingDTO.Req bookingReq) {
        Booking booking = bookingReq.toBooking();
        booking.setBookingId(id);

        // Generate the list of updated bookings based on the booking type
        List<Booking> updatedBookings = bookingService.handleBookingType(booking);

        // Check for conflicts with existing bookings
        for (Booking updatedBooking : updatedBookings) {
            if (bookingService.isConflict(updatedBooking)) {
                throw new InvalidArgumentException("Booking time conflicts with an existing booking.");
            }
        }

        // Update each booking in the database
        for (Booking updatedBooking : updatedBookings) {
            bookingService.updateBooking(updatedBooking);
        }
        return new Result("Success", "Booking updated successfully.");
    }

    @Operation(summary = "Check booking conflict")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conflict check success",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content) })
    @PostMapping("/check")
    public Result checkBookingConflict(@RequestBody @Valid BookingDTO.CheckReq checkReq) {
        Booking booking = checkReq.toBooking();
        boolean conflict = bookingService.isConflict(booking);
        return new Result("Success", conflict ? "Conflict" : "No conflict", String.valueOf(conflict));
    }

    @Operation(summary = "Delete booking")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking deleted",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Result.class)) }),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content) })
    @DeleteMapping("/{id}")
    public Result deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return new Result("Success", "Booking deleted successfully.");
    }
}