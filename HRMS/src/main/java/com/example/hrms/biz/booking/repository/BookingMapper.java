package com.example.hrms.biz.booking.repository;

import com.example.hrms.biz.booking.model.Booking;
import com.example.hrms.biz.booking.model.criteria.BookingCriteria;
import org.apache.ibatis.annotations.*;
import com.example.hrms.enumation.BookingType;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BookingMapper {
    @Select("SELECT * FROM Bookings " +
        "WHERE start_time BETWEEN #{start} AND #{end}")
    List<Booking> findBookingsInTimeRange(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    @Insert("INSERT INTO bookings (username, room_id, title, attendees, content, start_time, end_time, status, booking_type, weekdays) VALUES (#{username}, #{roomId}, #{title}, #{attendees}, #{content}, #{startTime}, #{endTime}, #{status}, #{bookingType}, #{weekdays})")
    void insert(Booking booking);

    @Select("SELECT booking_id, username, room_id, title, attendees, content, start_time, end_time, status, booking_type, weekdays FROM bookings WHERE booking_id = #{bookingId}")
    @Results({
            @Result(property = "bookingId", column = "booking_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "roomId", column = "room_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "attendees", column = "attendees"),
            @Result(property = "content", column = "content"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "bookingType", column = "booking_type"),
            @Result(property = "weekdays", column = "weekdays")
    })
    Booking selectById(@Param("bookingId") long bookingId);

    int count(BookingCriteria criteria);

    List<Booking> select(BookingCriteria criteria);

    // Trong BookingMapper.java, sửa phương thức findConflictingBookings:
    @Select("SELECT booking_id, username, room_id, title, attendees, content, start_time, end_time, status, booking_type, weekdays " +
                "FROM bookings WHERE room_id = #{roomId} AND ((start_time < #{endTime} AND end_time > #{startTime})) " +
                "AND booking_id != #{bookingId}")  // Thêm điều kiện này
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId, @Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime, @Param("bookingId") Long bookingId);

    @Update("UPDATE bookings SET username = #{username}, room_id = #{roomId}, title = #{title}, attendees = #{attendees}, content = #{content}, start_time = #{startTime}, end_time = #{endTime}, status = #{status}, booking_type = #{bookingType}, weekdays = #{weekdays} WHERE booking_id = #{bookingId}")
    void updateBooking(Booking booking);

    @Delete("DELETE FROM bookings WHERE booking_id = #{bookingId}")
    void deleteBooking(@Param("bookingId") Long bookingId);

    @Select("SELECT booking_id, username, room_id, title, attendees, content, start_time, end_time, status, booking_type, weekdays FROM bookings")
    @Results({
            @Result(property = "bookingId", column = "booking_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "roomId", column = "room_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "attendees", column = "attendees"),
            @Result(property = "content", column = "content"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "bookingType", column = "booking_type"),
            @Result(property = "weekdays", column = "weekdays")
    })
    List<Booking> selectAll();
    @Select("SELECT booking_id, username, room_id, title, attendees, content, start_time, end_time, status, booking_type, weekdays " +
            "FROM bookings WHERE username = #{username} AND start_time <= #{now} AND end_time >= #{now} OR attendees LIKE CONCAT('%', #{username}, '%')")
    @Results({
            @Result(property = "bookingId", column = "booking_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "roomId", column = "room_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "attendees", column = "attendees"),
            @Result(property = "content", column = "content"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "bookingType", column = "booking_type"),
            @Result(property = "weekdays", column = "weekdays")
    })
    List<Booking> findOngoingBookingsByUsername(@Param("username") String username, @Param("now") LocalDateTime now);

    @Select("SELECT booking_id, username, room_id, title, attendees, content, start_time, end_time, status, booking_type, weekdays " +
            "FROM bookings WHERE start_time > #{currentTime} AND (attendees LIKE CONCAT('%', #{username}, '%') OR username = #{username})")
    @Results({
            @Result(property = "bookingId", column = "booking_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "roomId", column = "room_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "attendees", column = "attendees"),
            @Result(property = "content", column = "content"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "bookingType", column = "booking_type"),
            @Result(property = "weekdays", column = "weekdays")
    })
    List<Booking> findUpcomingBookingsByAttendee(@Param("username") String username, @Param("currentTime") LocalDateTime currentTime);

}