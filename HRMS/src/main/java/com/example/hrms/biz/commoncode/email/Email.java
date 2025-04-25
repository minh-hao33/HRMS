package com.example.hrms.biz.commoncode.email;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class Email {
  private String to;
  private List<String> cc;
  private List<String> bcc;
  private String subject;
  private String content;
  private String from;
  private String replyTo;

  public Email() {
  }
}
