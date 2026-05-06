package com.hyeon.guardrail.common.config;

import com.hyeon.guardrail.common.exception.BaseExceptionHandlerFilter;
import com.hyeon.guardrail.common.security.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** 인증 구현 전 기본 보안 진입 비활성화 설정 */
@Configuration
public class SecurityConfig {

  @Value("${app.cors.allowed-origins}")
  private List<String> allowedOrigins;

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final BaseExceptionHandlerFilter baseExceptionHandlerFilter;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      BaseExceptionHandlerFilter baseExceptionHandlerFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.baseExceptionHandlerFilter = baseExceptionHandlerFilter;
  }

  /** 인증 구현 전 기본 보안 필터 체인 설정 */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**")
                    .permitAll()
                    .requestMatchers("/uploads/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/v1/auth/login", "/api/v1/auth/token/refresh", "/api/v1/auth/logout")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(baseExceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(jwtAuthenticationFilter, BaseExceptionHandlerFilter.class)
        .logout(Customizer.withDefaults());

    return http.build();
  }

  /** 프론트엔드 클라이언트 접근 허용용 CORS 설정 */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /** 비밀번호 해시 encoder 설정 */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
