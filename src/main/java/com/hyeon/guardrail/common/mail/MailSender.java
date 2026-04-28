package com.hyeon.guardrail.common.mail;

/** 메일 발송 추상화 */
public interface MailSender {

  /** 메일 발송 */
  void send(String to, String subject, String body);
}
