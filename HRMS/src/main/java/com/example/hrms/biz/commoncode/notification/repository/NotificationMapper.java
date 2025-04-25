package com.example.hrms.biz.commoncode.notification.repository;

import com.example.hrms.biz.commoncode.notification.model.Notification;
import com.example.hrms.biz.commoncode.notification.model.dto.NotificationDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

  @Insert("INSERT INTO Notifications(title, content, sender, receiver) VALUES(#{title}, #{content}, #{sender}, #{receiver})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Notification notification);

  @Select("SELECT n.*, u.employee_name as sender_name FROM Notifications n " +
      "JOIN Users u ON n.sender = u.username " +
      "WHERE n.receiver = #{receiver} " +
      "ORDER BY n.created_at DESC")
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "title", column = "title"),
      @Result(property = "content", column = "content"),
      @Result(property = "sender", column = "sender"),
      @Result(property = "senderName", column = "sender_name"),
      @Result(property = "receiver", column = "receiver"),
      @Result(property = "createdAt", column = "created_at"),
      @Result(property = "isRead", column = "is_read")
  })
  List<NotificationDTO> findByReceiver(String receiver);

  @Select("SELECT n.*, u.employee_name as sender_name FROM Notifications n " +
      "JOIN Users u ON n.sender = u.username " +
      "WHERE n.id = #{id}")
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "title", column = "title"),
      @Result(property = "content", column = "content"),
      @Result(property = "sender", column = "sender"),
      @Result(property = "senderName", column = "sender_name"),
      @Result(property = "receiver", column = "receiver"),
      @Result(property = "createdAt", column = "created_at"),
      @Result(property = "isRead", column = "is_read")
  })
  NotificationDTO findById(Long id);

  @Update("UPDATE Notifications SET is_read = true WHERE id = #{id}")
  int markAsRead(Long id);

  @Update("UPDATE Notifications SET is_read = true WHERE receiver = #{receiver} AND is_read = false")
  int markAllAsRead(String receiver);

  @Select("SELECT COUNT(*) FROM Notifications WHERE receiver = #{receiver} AND is_read = false")
  int countUnread(String receiver);

  @Delete("DELETE FROM Notifications WHERE id = #{id}")
  int delete(Long id);

  // Lấy danh sách thông báo của một phòng ban
  @Select("SELECT n.*, u.employee_name as sender_name FROM Notifications n " +
      "JOIN Users u ON n.sender = u.username " +
      "JOIN Users r ON n.receiver = r.username " +
      "WHERE r.department_id = #{departmentId} " +
      "ORDER BY n.created_at DESC")
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "title", column = "title"),
      @Result(property = "content", column = "content"),
      @Result(property = "sender", column = "sender"),
      @Result(property = "senderName", column = "sender_name"),
      @Result(property = "receiver", column = "receiver"),
      @Result(property = "createdAt", column = "created_at"),
      @Result(property = "isRead", column = "is_read")
  })
  List<NotificationDTO> findByDepartment(Long departmentId);
}