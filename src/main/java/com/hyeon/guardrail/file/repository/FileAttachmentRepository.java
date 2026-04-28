package com.hyeon.guardrail.file.repository;

import com.hyeon.guardrail.file.domain.FileAttachment;
import com.hyeon.guardrail.file.domain.FileTargetType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 파일 첨부 기본 영속성 저장소 */
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, UUID> {

  /** 삭제되지 않은 같은 대상의 정렬 순서 존재 여부 조회 */
  boolean existsByTargetTypeAndTargetIdAndSortOrderAndDeletedFalse(
      FileTargetType targetType, UUID targetId, int sortOrder);

  /** 특정 파일을 제외한 삭제되지 않은 같은 대상의 정렬 순서 존재 여부 조회 */
  boolean existsByTargetTypeAndTargetIdAndSortOrderAndDeletedFalseAndIdNot(
      FileTargetType targetType, UUID targetId, int sortOrder, UUID fileAttachmentId);

  /** 파일 첨부 기본 정보 수정 */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update FileAttachment fileAttachment
         set fileAttachment.fileName = :fileName,
             fileAttachment.originalFileName = :originalFileName,
             fileAttachment.filePath = :filePath,
             fileAttachment.fileSize = :fileSize,
             fileAttachment.contentType = :contentType,
             fileAttachment.sortOrder = :sortOrder
       where fileAttachment.id = :fileAttachmentId
         and fileAttachment.deleted = false
      """)
  int updateBasicInfo(
      @Param("fileAttachmentId") UUID fileAttachmentId,
      @Param("fileName") String fileName,
      @Param("originalFileName") String originalFileName,
      @Param("filePath") String filePath,
      @Param("fileSize") long fileSize,
      @Param("contentType") String contentType,
      @Param("sortOrder") int sortOrder);
}
