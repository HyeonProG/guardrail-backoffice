package com.hyeon.guardrail.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** JPA Auditing 기반 생성/수정 일시 자동 기록 활성화 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {}
