package com.example.hrms.biz.commoncode.notification.model.dto;

import com.example.hrms.biz.commoncode.notification.model.Notification;
import org.springframework.beans.BeanUtils;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class NotificationDTO {

  @Getter
  @Setter
  public static class Req {
    private Long id;
    private String title;
    private String content;
    private String sender;
    private String receiver;
    private boolean isRead;
    private String type;  // Add type field for notification type

    public static Req fromNotification(Notification notification) {
      Req req = new Req();
      BeanUtils.copyProperties(notification, req);
      return req;
    }

    public Notification toNotification() {
      Notification notification = new Notification();
      BeanUtils.copyProperties(this, notification);

      // Set default values if needed
      if (notification.getCreatedAt() == null) {
        notification.setCreatedAt(LocalDateTime.now());
      }

      // Set default type if not provided
      if (notification.getType() == null) {
        notification.setType("info");
      }

      return notification;
    }
  }

  @Getter
  @Setter
  public static class Resp {
    private Long id;
    private String title;
    private String content;
    private String sender;
    private String senderName;  // Tên người gửi (từ Users.employee_name)
    private String receiver;
    private LocalDateTime createdAt;
    private boolean isRead;
    private String timeAgo;     // Hiển thị thời gian tương đối
    private String type;        // Loại thông báo: info, success, warning, danger

    public static Resp toResponse(Notification notification) {
      Resp resp = new Resp();
      BeanUtils.copyProperties(notification, resp);

      // Add time ago calculation
      addTimeAgo(resp);

      // Default type if not set
      if (resp.getType() == null) {
        resp.setType("info");
      }

      return resp;
    }

    private static void addTimeAgo(Resp notification) {
      if (notification.getCreatedAt() == null) {
        notification.setTimeAgo("Vừa xong");
        return;
      }

      LocalDateTime now = LocalDateTime.now();
      LocalDateTime createdAt = notification.getCreatedAt();
      long minutes = ChronoUnit.MINUTES.between(createdAt, now);

      if (minutes < 1) {
        notification.setTimeAgo("Vừa xong");
      } else if (minutes < 60) {
        notification.setTimeAgo(minutes + " phút trước");
      } else if (minutes < 24 * 60) {
        long hours = minutes / 60;
        notification.setTimeAgo(hours + " giờ trước");
      } else {
        long days = minutes / (24 * 60);
        notification.setTimeAgo(days + " ngày trước");
      }
    }
  }

  // For bulk operations
  @Getter
  @Setter
  public static class BulkReq {
    private String title;
    private String content;
    private String sender;
    private String[] receivers;
    private String type; // Loại thông báo: info, success, warning, danger
  }

  // Legacy getters/setters for backward compatibility
  private Long id;
  private String title;
  private String content;
  private String sender;
  private String senderName;
  private String receiver;
  private LocalDateTime createdAt;
  private boolean isRead;
  private String timeAgo;
  private String type;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getSenderName() {
    return senderName;
  }

  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }

  public String getReceiver() {
    return receiver;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    isRead = read;
  }

  public String getTimeAgo() {
    return timeAgo;
  }

  public void setTimeAgo(String timeAgo) {
    this.timeAgo = timeAgo;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}