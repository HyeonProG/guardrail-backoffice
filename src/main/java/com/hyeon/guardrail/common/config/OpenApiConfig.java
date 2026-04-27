package com.hyeon.guardrail.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger/OpenAPI 문서 메타데이터와 보안 스키마 설정 */
@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI guardrailOpenApi(@Value("${app.api.version}") String appVersion) {
    return new OpenAPI()
        .info(
            new Info()
                .title("Guardrail API")
                .description("커머스 백오피스 하네스 기반 API 문서")
                .version(appVersion))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME_NAME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME));
  }
}
