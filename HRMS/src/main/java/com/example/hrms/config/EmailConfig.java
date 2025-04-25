package com.example.hrms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Properties;

@Configuration
@EnableAsync
public class EmailConfig {

  @Value("${mail.host:smtp.gmail.com}")
  private String host;

  @Value("${mail.port:587}")
  private int port;

  @Value("${mail.username:username}")
  private String username;

  @Value("${mail.password:password}")
  private String password;

  @Value("${mail.smtp.auth:true}")
  private String auth;

  @Value("${mail.smtp.starttls.enable:true}")
  private String starttlsEnable;

  @Value("${mail.smtp.debug:true}")
  private String debug;

  @Value("${mail.from:emailai.system@gmail.com}")
  private String fromEmail;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setUsername(username);
    mailSender.setPassword(password);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", auth);
    props.put("mail.smtp.starttls.enable", starttlsEnable);
    props.put("mail.debug", debug);
    props.put("mail.smtp.timeout", "5000");
    props.put("mail.smtp.connectiontimeout", "5000");
    props.put("mail.smtp.writetimeout", "5000");

    return mailSender;
  }

  @Bean
  public String fromEmail() {
    return fromEmail;
  }
}