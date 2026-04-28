package com.hyeon.guardrail.common.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 로깅 기반 임시 메일 발송 구현 */
@Slf4j
@Component
public class LoggingMailSender implements MailSender {

  /** 메일 발송 */
  @Override
  public void send(String to, String subject, String body) {
    log.info("임시 메일 발송 to={}, subject={}, body={}", to, subject, body);
  }
}
