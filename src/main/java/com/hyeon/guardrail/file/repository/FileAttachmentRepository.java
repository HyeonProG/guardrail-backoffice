package com.hyeon.guardrail.file.repository;

import com.hyeon.guardrail.file.domain.FileAttachment;
import com.hyeon.guardrail.file.domain.FileTargetType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 파일 첨부 기본 영속성 저장소 */
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, UUID> {

  /** 삭제되지 않은 같은 대상의 정렬 순서 존재 여부 조회 */
  boolean existsByTargetTypeAndTargetIdAndSortOrderAndDeletedFalse(
      FileTargetType targetType, UUID targetId, int sortOrder);

  /** 특정 파일을 제외한 삭제되지 않은 같은 대상의 정렬 순서 존재 여부 조회 */
  boolean existsByTargetTypeAndTargetIdAndSortOrderAndDeletedFalseAndIdNot(
      FileTargetType targetType, UUID targetId, int sortOrder, UUID fileAttachmentId);
}
