package com.example.hrms.biz.commoncode.notification.controller;

import com.example.hrms.biz.commoncode.notification.model.WebSocketNotification;
import com.example.hrms.biz.commoncode.notification.model.dto.NotificationDTO;
import com.example.hrms.biz.commoncode.notification.service.NotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller to handle WebSocket messages for notifications
 */
@Controller
public class WebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private final NotificationService notificationService;

  public WebSocketController(SimpMessagingTemplate messagingTemplate, NotificationService notificationService) {
    this.messagingTemplate = messagingTemplate;
    this.notificationService = notificationService;
  }

  /**
   * Handle direct notification from client to specific user
   * Client sends to: /app/notification.send
   */
  @MessageMapping("/notification.send")
  public void sendNotification(@Payload NotificationDTO.Req notification) {
    // Create the notification in the database
    NotificationDTO.Resp createdNotification = notificationService.createNotification(notification);

    // No need to send manually through WebSocket - the service already does this
  }

  /**
   * Handle bulk notifications from client
   * Client sends to: /app/notification.bulk
   */
  @MessageMapping("/notification.bulk")
  public void sendBulkNotification(@Payload NotificationDTO.BulkReq request) {
    // Create bulk notifications - service handles WebSocket sending
    notificationService.createBulkNotifications(request);
  }

  /**
   * Handle direct message (not persisted) between users
   * Client sends to: /app/message.send
   */
  @MessageMapping("/message.send")
  public void sendDirectMessage(@Payload WebSocketMessage message) {
    // Send direct message without persisting
    messagingTemplate.convertAndSendToUser(
        message.getReceiver(),
        "/queue/messages",
        new WebSocketNotification("NEW_MESSAGE", message)
    );
  }

  /**
   * Simple message POJO for direct messaging
   */
  static class WebSocketMessage {
    private String sender;
    private String receiver;
    private String content;
    private String type;

    // Getters and setters
    public String getSender() {
      return sender;
    }

    public void setSender(String sender) {
      this.sender = sender;
    }

    public String getReceiver() {
      return receiver;
    }

    public void setReceiver(String receiver) {
      this.receiver = receiver;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }
}