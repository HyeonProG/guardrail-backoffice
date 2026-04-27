package com.hyeon.guardrail.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** 인증 구현 전 기본 보안 진입 비활성화 설정 */
@Configuration
public class SecurityConfig {

  /** 인증 구현 전 기본 보안 필터 체인 설정 */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .permitAll())
        .logout(Customizer.withDefaults());

    return http.build();
  }

  /** 비밀번호 해시 encoder 설정 */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
