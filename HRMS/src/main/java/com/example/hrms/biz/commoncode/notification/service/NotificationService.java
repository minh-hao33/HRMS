package com.example.hrms.biz.commoncode.notification.service;

import com.example.hrms.biz.commoncode.email.EmailService;
import com.example.hrms.biz.commoncode.notification.model.Notification;
import com.example.hrms.biz.commoncode.notification.model.dto.NotificationDTO;
import com.example.hrms.biz.commoncode.notification.repository.NotificationMapper;
import com.example.hrms.biz.commoncode.notification.model.WebSocketNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
public class NotificationService {

  private final NotificationMapper notificationMapper;
  private final SimpMessagingTemplate messagingTemplate;
  private final EmailService emailService;

  public NotificationService(NotificationMapper notificationMapper, SimpMessagingTemplate messagingTemplate, EmailService emailService){
    this.notificationMapper = notificationMapper;
    this.messagingTemplate = messagingTemplate;
    this.emailService = emailService;
  }

  /**
   * Tạo thông báo mới
   */
  public Notification createNotification(Notification notification) {
    // Đảm bảo createdAt được thiết lập
    if (notification.getCreatedAt() == null) {
      notification.setCreatedAt(LocalDateTime.now());
    }

    // Đảm bảo loại thông báo mặc định là 'info' nếu không được chỉ định
    if (notification.getType() == null) {
      notification.setType("info");
    }

    // Lưu thông báo vào cơ sở dữ liệu
    notificationMapper.insert(notification);

    // Lấy thông báo đã được insert vào database (có ID)
    NotificationDTO latestNotification = notificationMapper.findById(notification.getId());

    // Nếu không tìm thấy, sử dụng thông báo gốc
    if (latestNotification == null) {
      latestNotification = new NotificationDTO();
      latestNotification.setId(notification.getId());
      latestNotification.setTitle(notification.getTitle());
      latestNotification.setContent(notification.getContent());
      latestNotification.setReceiver(notification.getReceiver());
    }

    // Gửi email thông báo
    try {
      emailService.sendNotificationEmail(notification);
    } catch (Exception e) {
      log.warn("Email notification failed: {}", e.getMessage());
    }

    // Gửi thông báo qua WebSocket đến người nhận cụ thể
    NotificationDTO.Resp dto = convertToResp(latestNotification);
    messagingTemplate.convertAndSendToUser(
        notification.getReceiver(),
        "/queue/notifications",
        new WebSocketNotification("NEW_NOTIFICATION", dto)
    );

    return notification;
  }

  /**
   * Tạo thông báo từ Req DTO
   */
  public NotificationDTO.Resp createNotification(NotificationDTO.Req request) {
    Notification notification = request.toNotification();
    createNotification(notification);
    return NotificationDTO.Resp.toResponse(notification);
  }

  /**
   * Lấy danh sách thông báo của người dùng
   */
  public List<NotificationDTO.Resp> getNotificationsByReceiver(String receiver) {
    List<NotificationDTO> notifications = notificationMapper.findByReceiver(receiver);
    return notifications.stream()
        .map(this::convertToResp)
        .collect(Collectors.toList());
  }

  /**
   * Lấy thông tin chi tiết của một thông báo
   */
  public NotificationDTO.Resp getNotificationById(Long id) {
    NotificationDTO notification = notificationMapper.findById(id);
    if (notification != null) {
      return convertToResp(notification);
    }
    return null;
  }

  /**
   * Đánh dấu thông báo đã đọc và gửi cập nhật WebSocket
   */
  public void markAsRead(Long id) {
    NotificationDTO notification = notificationMapper.findById(id);
    if (notification == null) {
      return; // Không tìm thấy thông báo
    }

    notificationMapper.markAsRead(id);

    // Gửi cập nhật trạng thái qua WebSocket
    messagingTemplate.convertAndSendToUser(
        notification.getReceiver(),
        "/queue/notifications",
        new WebSocketNotification("MARK_READ", id)
    );
  }

  /**
   * Đánh dấu tất cả thông báo đã đọc và gửi cập nhật WebSocket
   */
  public void markAllAsRead(String receiver) {
    notificationMapper.markAllAsRead(receiver);

    // Gửi cập nhật trạng thái qua WebSocket
    messagingTemplate.convertAndSendToUser(
        receiver,
        "/queue/notifications",
        new WebSocketNotification("MARK_ALL_READ", null)
    );
  }

  /**
   * Lấy số lượng thông báo chưa đọc
   */
  public int getUnreadCount(String receiver) {
    return notificationMapper.countUnread(receiver);
  }

  /**
   * Xóa thông báo và gửi cập nhật WebSocket
   */
  public void deleteNotification(Long id) {
    NotificationDTO notification = notificationMapper.findById(id);
    if (notification == null) {
      return; // Không tìm thấy thông báo
    }

    notificationMapper.delete(id);

    // Gửi cập nhật xóa thông báo qua WebSocket
    messagingTemplate.convertAndSendToUser(
        notification.getReceiver(),
        "/queue/notifications",
        new WebSocketNotification("DELETE_NOTIFICATION", id)
    );
  }

  /**
   * Lấy danh sách thông báo của một phòng ban
   */
  public List<NotificationDTO.Resp> getNotificationsByDepartment(Long departmentId) {
    List<NotificationDTO> notifications = notificationMapper.findByDepartment(departmentId);
    return notifications.stream()
        .map(this::convertToResp)
        .collect(Collectors.toList());
  }

  /**
   * Chuyển đổi NotificationDTO legacy sang NotificationDTO.Resp
   */
  private NotificationDTO.Resp convertToResp(NotificationDTO oldDto) {
    if (oldDto == null) return null;

    NotificationDTO.Resp resp = new NotificationDTO.Resp();
    resp.setId(oldDto.getId());
    resp.setTitle(oldDto.getTitle());
    resp.setContent(oldDto.getContent());
    resp.setSender(oldDto.getSender());
    resp.setSenderName(oldDto.getSenderName());
    resp.setReceiver(oldDto.getReceiver());
    resp.setCreatedAt(oldDto.getCreatedAt());
    resp.setRead(oldDto.isRead());

    // Thiết lập timeAgo
    if (oldDto.getTimeAgo() != null) {
      resp.setTimeAgo(oldDto.getTimeAgo());
    } else if (oldDto.getCreatedAt() != null) {
      resp.setTimeAgo(calculateTimeAgo(oldDto.getCreatedAt()));
    } else {
      resp.setTimeAgo("Vừa xong");
    }

    // Thiết lập type
    resp.setType(oldDto.getType() != null ? oldDto.getType() : "info");

    return resp;
  }

  /**
   * Tính toán thời gian tương đối
   */
  private String calculateTimeAgo(LocalDateTime createdAt) {
    if (createdAt == null) {
      return "Vừa xong";
    }

    LocalDateTime now = LocalDateTime.now();
    long minutes = ChronoUnit.MINUTES.between(createdAt, now);

    if (minutes < 1) {
      return "Vừa xong";
    } else if (minutes < 60) {
      return minutes + " phút trước";
    } else if (minutes < 24 * 60) {
      long hours = minutes / 60;
      return hours + " giờ trước";
    } else {
      long days = minutes / (24 * 60);
      return days + " ngày trước";
    }
  }

  /**
   * Tạo thông báo cho nhiều người nhận với WebSocket
   */
  public void createBulkNotifications(String title, String content, String sender, List<String> receivers) {
    for (String receiver : receivers) {
      Notification notification = new Notification();
      notification.setTitle(title);
      notification.setContent(content);
      notification.setSender(sender);
      notification.setReceiver(receiver);
      notification.setCreatedAt(LocalDateTime.now());
      createNotification(notification);
    }
  }

  /**
   * Tạo thông báo từ BulkReq DTO
   */
  public int createBulkNotifications(NotificationDTO.BulkReq request) {
    if (request.getReceivers() == null || request.getReceivers().length == 0) {
      return 0;
    }

    for (String receiver : request.getReceivers()) {
      Notification notification = new Notification();
      notification.setTitle(request.getTitle());
      notification.setContent(request.getContent());
      notification.setSender(request.getSender());
      notification.setReceiver(receiver);
      notification.setCreatedAt(LocalDateTime.now());
      notification.setType(request.getType() != null ? request.getType() : "info");
      createNotification(notification);
    }

    return request.getReceivers().length;
  }

  /**
   * Tạo thông báo cho toàn bộ phòng ban
   */
  public void createDepartmentNotification(String title, String content, String sender, Long departmentId,
      List<String> usernames) {
    for (String username : usernames) {
      Notification notification = new Notification();
      notification.setTitle(title);
      notification.setContent(content);
      notification.setSender(sender);
      notification.setReceiver(username);
      notification.setCreatedAt(LocalDateTime.now());
      createNotification(notification);
    }
  }
}