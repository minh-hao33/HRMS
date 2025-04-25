package com.example.hrms.biz.booking.service;

import com.example.hrms.biz.booking.model.Booking;
import com.example.hrms.biz.booking.model.criteria.BookingCriteria;
import com.example.hrms.biz.booking.model.dto.BookingDTO;
import com.example.hrms.biz.booking.repository.BookingMapper;
import com.example.hrms.biz.commoncode.notification.service.NotificationService;
import com.example.hrms.exception.InvalidArgumentException;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.*;
import java.util.*;

import com.example.hrms.enumation.BookingType;

@Service
public class BookingService {
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;

    public BookingService(BookingMapper bookingMapper, NotificationService notificationService) {
        this.bookingMapper = bookingMapper;
        this.notificationService = notificationService;
    }

    public Booking getBookingById(Long bookingId) {
        return bookingMapper.selectById(bookingId);
    }

    public List<BookingDTO.Resp> getOngoingBookings(String username) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> ongoingBookings = bookingMapper.findOngoingBookingsByUsername(username, now);
        return ongoingBookings.stream().map(BookingDTO.Resp::toResponse).collect(Collectors.toList());
    }

    // This method returns the upcoming bookings as BookingDTO.Resp
    public List<BookingDTO.Resp> getUpcomingBookings(String username) {
        // Get current time
        LocalDateTime now = LocalDateTime.now();

        // Get the list of upcoming bookings
        List<Booking> bookings = bookingMapper.findUpcomingBookingsByUsername(username, now);

        // Convert the list of Booking entities to BookingDTO.Resp and return
        return bookings.stream()
                .map(BookingDTO.Resp::toResponse) // Assuming there's a static method `toResponse` in `BookingDTO.Resp` class
                .collect(Collectors.toList());
    }

    public void insert(BookingDTO.Req req) {
        Booking booking = req.toBooking();
        List<Booking> bookings = handleBookingType(booking);
        for (Booking b : bookings) {
            bookingMapper.insert(b);

            // Send notification to meeting organizer
            sendBookingNotification(b, "New Meeting Created",
                "You have created a new meeting: " + b.getTitle(), b.getUsername());

            // Send notifications to all attendees if available
            notifyAttendees(b, "Meeting Invitation",
                "You have been invited to a meeting: " + b.getTitle() + " by " + b.getUsername());
        }
    }

    public void updateBooking(Booking booking) {
        List<Booking> bookings = handleBookingType(booking);
        for (Booking b : bookings) {
            bookingMapper.updateBooking(b);

            // Send notification about the update
            sendBookingNotification(b, "Meeting Updated",
                "A meeting has been updated: " + b.getTitle(), b.getUsername());

            // Notify attendees about the update
            notifyAttendees(b, "Meeting Update",
                "A meeting you are attending has been updated: " + b.getTitle() + " by " + b.getUsername());
        }
    }

    private void sendBookingNotification(Booking booking, String title, String content, String receiver) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedStartTime = booking.getStartTime().format(formatter);
        String formattedEndTime = booking.getEndTime().format(formatter);

        String fullContent = content + "\nTime: " + formattedStartTime + " to " + formattedEndTime;

        notificationService.createNotification(
            com.example.hrms.biz.commoncode.notification.model.Notification.builder()
                .title(title)
                .content(fullContent)
                .sender("system")
                .receiver(receiver)
                .type("meeting")
                .createdAt(LocalDateTime.now())
                .build()
        );
    }

    private void notifyAttendees(Booking booking, String title, String content) {
        if (booking.getAttendees() == null || booking.getAttendees().isEmpty()) {
            return;
        }

        List<String> attendees = Arrays.asList(booking.getAttendees().split(","))
            .stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty() && !s.equals(booking.getUsername())) // Don't notify the creator twice
            .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTime = booking.getStartTime().format(formatter);
        String fullContent = content + "\nTime: " + formattedTime;

        if (!attendees.isEmpty()) {
            notificationService.createBulkNotifications(
                title,
                fullContent,
                booking.getUsername(),
                attendees
            );
        }
    }


    public void deleteBooking(Long bookingId) {
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking != null) {
            // Notify owner and attendees about cancellation
            sendBookingNotification(booking, "Meeting Cancelled",
                "Your meeting has been cancelled: " + booking.getTitle(), booking.getUsername());

            notifyAttendees(booking, "Meeting Cancellation",
                "A meeting you were attending has been cancelled: " + booking.getTitle() + " by " + booking.getUsername());
        }

        bookingMapper.deleteBooking(bookingId);
    }

    public boolean isConflict(Booking booking) {
        List<Booking> conflictingBookings = bookingMapper.findConflictingBookings(
                booking.getRoomId(), booking.getStartTime(), booking.getEndTime()
        );
        return !conflictingBookings.isEmpty();
    }

    public int count(BookingCriteria criteria) {
        return bookingMapper.count(criteria);
    }

    public List<BookingDTO.Resp> list(BookingCriteria criteria) {
        List<Booking> bookings = bookingMapper.select(criteria);
        return bookings.stream().map(BookingDTO.Resp::toResponse).toList();
    }

    public List<BookingDTO.Resp> getAllBookings() {
        List<Booking> bookings = bookingMapper.selectAll();
        return bookings.stream().map(BookingDTO.Resp::toResponse).toList();
    }

    public List<Booking> handleBookingType(Booking booking) {
        List<Booking> generatedBookings = new ArrayList<>();

        if (booking.getBookingType() == null) {
            booking.setBookingType(BookingType.ONLY);
        }

        LocalTime startTime = booking.getStartTime().toLocalTime();
        LocalTime endTime = booking.getEndTime().toLocalTime();

        switch (booking.getBookingType()) {
            case ONLY:
                // Gán lại ngày hiện tại nếu người dùng chỉ chọn giờ
                LocalDate today = LocalDate.now();
                booking.setStartTime(LocalDateTime.of(today, startTime));
                booking.setEndTime(LocalDateTime.of(today, endTime));
                booking.setWeekdays(null);
                generatedBookings.add(booking);
                break;

            case DAILY:
                // Sinh booking mỗi ngày trong khoảng từ ngày bắt đầu đến ngày kết thúc
                LocalDate dailyStart = booking.getStartTime().toLocalDate();
                LocalDate dailyEnd = booking.getEndTime().toLocalDate();

                for (LocalDate date = dailyStart; !date.isAfter(dailyEnd); date = date.plusDays(1)) {
                    Booking b = copyBooking(booking);
                    b.setStartTime(LocalDateTime.of(date, startTime));
                    b.setEndTime(LocalDateTime.of(date, endTime));
                    b.setBookingType(BookingType.DAILY);
                    b.setWeekdays(null);
                    generatedBookings.add(b);
                }
                break;

            case WEEKLY:
                // WEEKLY cần chuỗi weekdays hợp lệ
                if (booking.getWeekdays() == null || booking.getWeekdays().isBlank()) {
                    throw new InvalidArgumentException("WEEKLY booking requires weekdays.");
                }

                Set<DayOfWeek> selectedDays = parseWeekdays(booking.getWeekdays());
                LocalDate weeklyStart = booking.getStartTime().toLocalDate();
                LocalDate weeklyEnd = booking.getEndTime().toLocalDate();

                for (LocalDate date = weeklyStart; !date.isAfter(weeklyEnd); date = date.plusDays(1)) {
                    if (selectedDays.contains(date.getDayOfWeek())) {
                        Booking b = copyBooking(booking);
                        b.setStartTime(LocalDateTime.of(date, startTime));
                        b.setEndTime(LocalDateTime.of(date, endTime));
                        b.setBookingType(BookingType.WEEKLY);
                        b.setWeekdays(booking.getWeekdays());
                        generatedBookings.add(b);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid booking type.");
        }

        return generatedBookings;
    }

    // Tạo bản sao Booking (copy thông tin chung)
    private Booking copyBooking(Booking original) {
        Booking b = new Booking();
        b.setUsername(original.getUsername());
        b.setRoomId(original.getRoomId());
        b.setTitle(original.getTitle());
        b.setAttendees(original.getAttendees());
        b.setContent(original.getContent());
        b.setStatus(original.getStatus());
        return b;
    }

    // Chuyển chuỗi weekdays "Mo,We,Fr" thành Set<DayOfWeek>
    private Set<DayOfWeek> parseWeekdays(String weekdays) {
        Map<String, DayOfWeek> map = Map.of(
                "Mo", DayOfWeek.MONDAY,
                "Tu", DayOfWeek.TUESDAY,
                "We", DayOfWeek.WEDNESDAY,
                "Th", DayOfWeek.THURSDAY,
                "Fr", DayOfWeek.FRIDAY,
                "Sa", DayOfWeek.SATURDAY,
                "Su", DayOfWeek.SUNDAY
        );

        return Arrays.stream(weekdays.split(","))
                .map(String::trim)
                .map(map::get)
                .collect(Collectors.toSet());
    }
}
