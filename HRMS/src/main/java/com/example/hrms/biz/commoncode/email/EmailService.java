package com.example.hrms.biz.commoncode.email;

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

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

  public EmailService(JavaMailSender mailSender, String fromEmail) {
    this.mailSender = mailSender;
    this.defaultFromEmail = fromEmail;
  }

  /**
   * Send a simple plain-text email
   */
  @Async
  public CompletableFuture<Boolean> sendEmail(String to, String subject, String content) {
    if (!validateEmail(to)) {
      log.error("Invalid email: {}", to);
      return CompletableFuture.completedFuture(false);
    }

    try {
      log.info("Sending email to: {}", to);
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(defaultFromEmail);
      message.setTo(to);
      message.setSubject(subject != null ? subject : "Notification");
      message.setText(content != null ? content : "");

      mailSender.send(message);
      log.info("Email sent successfully to: {}", to);
      return CompletableFuture.completedFuture(true);
    } catch (IllegalArgumentException e) {
      log.error("Failed to send email to {}: {}", to, e.getMessage());
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Send email with detailed information
   */
  @Async
  public CompletableFuture<Boolean> sendEmail(Email email) {
    if (email == null || isEmpty(email.getTo()) || !validateEmail(email.getTo())) {
      log.error("Invalid email or empty recipient");
      return CompletableFuture.completedFuture(false);
    }

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
      return CompletableFuture.completedFuture(true);
    } catch (IllegalArgumentException e) {
      log.error("Failed to send email to {}: {}", email.getTo(), e.getMessage());
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Alternative method - send email synchronously
   */
  public boolean sendEmailSync(String to, String subject, String content) {
    if (!validateEmail(to)) {
      log.error("Invalid email: {}", to);
      return false;
    }

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
    } catch (IllegalArgumentException e) {
      log.error("Failed to send email to {}: {}", to, e.getMessage());
      return false;
    }
  }

  /**
   * Alternative method - send detailed email synchronously
   */
  public boolean sendEmailSync(Email email) {
    if (email == null || isEmpty(email.getTo()) || !validateEmail(email.getTo())) {
      log.error("Invalid email or empty recipient");
      return false;
    }

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
    } catch (IllegalArgumentException e) {
      log.error("Failed to send email to {}: {}", email.getTo(), e.getMessage());
      return false;
    }
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