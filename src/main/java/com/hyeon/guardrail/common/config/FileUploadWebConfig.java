package com.hyeon.guardrail.common.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 업로드 파일 정적 리소스 노출 설정 */
@Configuration
public class FileUploadWebConfig implements WebMvcConfigurer {

  @Value("${app.upload.storage-root-path}")
  private String storageRootPath;

  @Value("${app.upload.public-url-prefix}")
  private String publicUrlPrefix;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path absoluteStorageRoot = Paths.get(storageRootPath).toAbsolutePath().normalize();
    String resourcePattern =
        publicUrlPrefix.endsWith("/**") ? publicUrlPrefix : publicUrlPrefix + "/**";
    String resourceLocation = absoluteStorageRoot.toUri().toString();

    registry.addResourceHandler(resourcePattern).addResourceLocations(resourceLocation);
  }
}
