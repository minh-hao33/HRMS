package com.example.hrms.biz.commoncode.notification.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
  private Long id;
  private String title;
  private String content;
  private String sender;    // Liên kết với username trong Users
  private String receiver;  // Liên kết với username trong Users
  private LocalDateTime createdAt;
  private boolean isRead;
  private String type;  // Add type field for different notification types (request, meeting, etc.)
}