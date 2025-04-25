package com.example.hrms.biz.commoncode.notification.controller.rest;

import com.example.hrms.biz.commoncode.notification.model.Notification;
import com.example.hrms.biz.commoncode.notification.model.dto.NotificationDTO;
import com.example.hrms.biz.commoncode.notification.service.NotificationService;
import com.example.hrms.common.http.model.ResultData;
import com.example.hrms.common.http.model.Status;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationRestController {

  private final NotificationService notificationService;

  public NotificationRestController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * Tạo mới thông báo
   */
  @PostMapping
  public ResultData<NotificationDTO.Resp> createNotification(@RequestBody NotificationDTO.Req request) {
    NotificationDTO.Resp created = notificationService.createNotification(request);
    return new ResultData<>(Status.SUCCESS.name(), created);
  }

  /**
   * Tạo thông báo cho nhiều người nhận
   */
  @PostMapping("/bulk")
  public ResultData<Map<String, Object>> createBulkNotifications(
      @RequestBody NotificationDTO.BulkReq request) {
    int count = notificationService.createBulkNotifications(request);
    return new ResultData<>(Status.SUCCESS.name(), Map.of(
        "message", "Đã gửi thông báo cho " + count + " người nhận",
        "count", count
    ));
  }

  /**
   * Lấy danh sách thông báo của người dùng
   */
  @GetMapping("/user/{username}")
  public ResultData<List<NotificationDTO.Resp>> getNotifications(@PathVariable String username) {
    List<NotificationDTO.Resp> notifications = notificationService.getNotificationsByReceiver(username);
    return new ResultData<>(Status.SUCCESS.name(), notifications);
  }

  /**
   * Lấy chi tiết thông báo
   */
  @GetMapping("/{id}")
  public ResultData<NotificationDTO.Resp> getNotificationById(@PathVariable Long id) {
    NotificationDTO.Resp notification = notificationService.getNotificationById(id);
    return new ResultData<>(Status.SUCCESS.name(), notification);
  }

  /**
   * Đánh dấu thông báo đã đọc
   */
  @PutMapping("/{id}/read")
  public ResultData<Void> markAsRead(@PathVariable Long id) {
    notificationService.markAsRead(id);
    return new ResultData<>(Status.SUCCESS.name());
  }

  /**
   * Đánh dấu tất cả thông báo đã đọc
   */
  @PutMapping("/user/{username}/read-all")
  public ResultData<Void> markAllAsRead(@PathVariable String username) {
    notificationService.markAllAsRead(username);
    return new ResultData<>(Status.SUCCESS.name());
  }

  /**
   * Lấy số lượng thông báo chưa đọc
   */
  @GetMapping("/user/{username}/count")
  public ResultData<Map<String, Integer>> getUnreadCount(@PathVariable String username) {
    int count = notificationService.getUnreadCount(username);
    return new ResultData<>(Status.SUCCESS.name(), Map.of("count", count));
  }

  /**
   * Xóa thông báo
   */
  @DeleteMapping("/{id}")
  public ResultData<Void> deleteNotification(@PathVariable Long id) {
    notificationService.deleteNotification(id);
    return new ResultData<>(Status.SUCCESS.name());
  }

  /**
   * Lấy danh sách thông báo của phòng ban
   */
  @GetMapping("/department/{departmentId}")
  public ResultData<List<NotificationDTO.Resp>> getNotificationsByDepartment(@PathVariable Long departmentId) {
    List<NotificationDTO.Resp> notifications = notificationService.getNotificationsByDepartment(departmentId);
    return new ResultData<>(Status.SUCCESS.name(), notifications);
  }
}