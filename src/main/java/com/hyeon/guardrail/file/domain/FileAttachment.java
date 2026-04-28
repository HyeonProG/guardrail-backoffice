package com.hyeon.guardrail.file.domain;

import com.hyeon.guardrail.common.domain.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 파일 첨부 메타데이터 엔티티 */
@Getter
@Entity
@Table(name = "file_attachments")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileAttachment extends SoftDeleteEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false)
  private FileTargetType targetType;

  @Column(name = "target_id", nullable = false)
  private UUID targetId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "original_file_name", nullable = false)
  private String originalFileName;

  @Column(name = "file_path", nullable = false)
  private String filePath;

  @Column(name = "file_size", nullable = false)
  private long fileSize;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private FileStatus status;

  /** 파일 상태를 운영 활성으로 변경 */
  public void activate() {
    this.status = FileStatus.ACTIVE;
  }

  /** 파일 상태를 운영 비활성으로 변경 */
  public void inactivate() {
    this.status = FileStatus.INACTIVE;
  }
}
