package com.example.hrms.biz.commoncode.email;

import com.example.hrms.biz.commoncode.notification.model.Notification;
import com.example.hrms.biz.user.repository.UserMapper;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;
  private final String defaultFromEmail;
  private final UserMapper userMapper;

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

  public EmailService(JavaMailSender mailSender, String fromEmail, UserMapper userMapper) {
    this.mailSender = mailSender;
    this.defaultFromEmail = fromEmail;
    this.userMapper = userMapper;
  }

  /**
   * Send a simple plain-text email asynchronously
   */
  @Async
  public CompletableFuture<Boolean> sendEmail(String to, String subject, String content) {
    if (!validateEmail(to)) {
      log.error("Invalid email: {}", to);
      return CompletableFuture.completedFuture(false);
    }

    return CompletableFuture.supplyAsync(() -> {
      try {
        log.info("Sending email to: {}", to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(defaultFromEmail);
        message.setTo(to);
        message.setSubject(subject != null ? subject : "Notification");
        message.setText(content != null ? content : "");

        mailSender.send(message);
        log.info("Email sent successfully to: {}", to);
        return true;
      } catch (Exception e) {
        log.error("Failed to send email to {}: {}", to, e.getMessage());
        return false;
      }
    });
  }

  /**
   * Send email with detailed information asynchronously
   */
  @Async
  public CompletableFuture<Boolean> sendEmail(Email email) {
    if (email == null || isEmpty(email.getTo()) || !validateEmail(email.getTo())) {
      log.error("Invalid email or empty recipient");
      return CompletableFuture.completedFuture(false);
    }

    return CompletableFuture.supplyAsync(() -> {
      try {
        log.info("Sending email to: {}", email.getTo());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(isEmpty(email.getFrom()) ? defaultFromEmail : email.getFrom());
        message.setTo(email.getTo());

        if (email.getCc() != null && !email.getCc().isEmpty()) {
          message.setCc(email.getCc().toArray(new String[0]));
        }

        if (email.getBcc() != null && !email.getBcc().isEmpty()) {
          message.setBcc(email.getBcc().toArray(new String[0]));
        }

        message.setSubject(isEmpty(email.getSubject()) ? "Notification" : email.getSubject());
        message.setText(email.getContent() != null ? email.getContent() : "");

        if (!isEmpty(email.getReplyTo())) {
          message.setReplyTo(email.getReplyTo());
        }

        mailSender.send(message);
        log.info("Email sent successfully to: {}", email.getTo());
        return true;
      } catch (Exception e) {
        log.error("Failed to send email to {}: {}", email.getTo(), e.getMessage());
        return false;
      }
    });
  }

  /**
   * Wrapper for backward compatibility - converts synchronous calls to async
   */
  public boolean sendEmailSync(String to, String subject, String content) {
    // Start the async process but don't wait for it
    sendEmail(to, subject, content);
    // Return true to maintain API compatibility
    return true;
  }

  /**
   * Wrapper for backward compatibility - converts synchronous calls to async
   */
  public boolean sendEmailSync(Email email) {
    // Start the async process but don't wait for it
    sendEmail(email);
    // Return true to maintain API compatibility
    return true;
  }

  /**
   * Send notification email asynchronously
   */
  @Async
  public CompletableFuture<Boolean> sendNotificationEmail(Notification notification) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Tìm email của người nhận
        String recipientEmail = userMapper.findEmailByUsername(notification.getReceiver());

        if (recipientEmail != null && validateEmail(recipientEmail)) {
          SimpleMailMessage message = new SimpleMailMessage();
          message.setFrom(defaultFromEmail);
          message.setTo(recipientEmail);
          message.setSubject(notification.getTitle());
          message.setText(notification.getContent());

          mailSender.send(message);
          log.info("Email notification sent to: {}", recipientEmail);
          return true;
        } else {
          log.warn("Could not send notification email: Invalid or missing email for user {}",
              notification.getReceiver());
          return false;
        }
      } catch (Exception e) {
        log.error("Failed to send notification email", e);
        return false;
      }
    });
  }

  /**
   * Validate email format
   */
  public boolean validateEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
  }

  /**
   * Check if a string is empty
   */
  private boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }
}