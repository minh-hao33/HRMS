package com.example.hrms.biz.commoncode.email;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

public class EmailServiceTest {

  @Mock
  private JavaMailSender mailSender;

  @InjectMocks
  private EmailService emailService;

  private String defaultFromEmail = "bkkhanh@cmcglobal.vn";

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    emailService = new EmailService(mailSender, defaultFromEmail);
  }

  @Test
  public void testValidateEmail_validEmail() {
    assertTrue(emailService.validateEmail("bkkhanh@cmcglobal.vn"));
  }

  @Test
  public void testValidateEmail_invalidEmail() {
    assertFalse(emailService.validateEmail("invalid-email"));
  }

  @Test
  public void testSendEmail_success() {
    String to = "bkkhanh@cmcglobal.vn";
    String subject = "Test Subject";
    String content = "Test Content";

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(defaultFromEmail);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(content);

    doNothing().when(mailSender).send(any(SimpleMailMessage.class));

    assertTrue(emailService.sendEmailSync(to, subject, content));
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  public void testSendEmail_invalidEmail() {
    String to = "invalid-email";
    String subject = "Test Subject";
    String content = "Test Content";

    assertFalse(emailService.sendEmailSync(to, subject, content));
    verify(mailSender, times(0)).send(any(SimpleMailMessage.class));
  }
  @Test
  public void testSendEmail_emptySubjectAndContent() {
    String to = "bkkhanh@cmcglobal.vn";
    assertTrue(emailService.sendEmailSync(to, null, null));
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  public void testSendEmail_withInvalidCc() {
    Email email = new Email();
    email.setTo("bkkhanh@cmcglobal.vn");
    email.setCc(List.of("invalid-email")); // CC không hợp lệ
    email.setSubject("Test");
    email.setContent("Test Content");

    assertFalse(emailService.sendEmailSync(email));
    verify(mailSender, times(0)).send(any(SimpleMailMessage.class));
  }

  @Test
  public void testSendEmailAsync_validEmail() {
    String to = "bkkhanh@cmcglobal.vn";
    CompletableFuture<Boolean> result = emailService.sendEmail(to, "Subject", "Content");
    assertTrue(result.join());
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  public void testSendEmail_withMultipleRecipients() {
    Email email = new Email();
    email.setTo("bkkhanh@cmcglobal.vn");
    email.setCc(List.of("ntdu@cmcglobal.vn", "pmhao@cmcglobal.vn"));
    email.setBcc(List.of("pnminh@cmcglobal.vn", "nhtien1@cmcglobal.vn"));
    email.setSubject("Subject");
    email.setContent("Content");

    assertTrue(emailService.sendEmailSync(email));
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }
  @Test
  public void testSendEmail_nullEmail() {
    assertFalse(emailService.sendEmailSync(null, "Subject", "Content"));
  }
}