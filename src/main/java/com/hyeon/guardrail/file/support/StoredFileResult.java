package com.hyeon.guardrail.file.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 저장소 업로드 결과 */
@Getter
@AllArgsConstructor
public class StoredFileResult {

  private String storedFileName;
  private String filePath;
  private String objectKey;
}
