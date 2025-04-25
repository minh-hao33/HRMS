package com.example.hrms.biz.commoncode.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketNotification {
  private String action; // Action type: NEW_NOTIFICATION, MARK_READ, DELETE, etc.
  private Object data; // Notification data payload
}