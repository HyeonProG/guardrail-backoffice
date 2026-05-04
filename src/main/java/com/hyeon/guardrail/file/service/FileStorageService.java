package com.hyeon.guardrail.file.service;

import com.hyeon.guardrail.file.domain.FileTargetType;
import com.hyeon.guardrail.file.support.StoredFileResult;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/** 외부 파일 저장소 업로드 처리 */
public interface FileStorageService {

  /** 파일을 저장소에 업로드한다. */
  StoredFileResult upload(FileTargetType targetType, UUID targetId, MultipartFile file);
}
