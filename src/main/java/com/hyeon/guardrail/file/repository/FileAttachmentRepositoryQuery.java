package com.hyeon.guardrail.file.repository;

import com.hyeon.guardrail.file.domain.FileAttachment;
import com.hyeon.guardrail.file.domain.FileTargetType;
import com.hyeon.guardrail.file.domain.QFileAttachment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** 파일 첨부 Querydsl 조회 저장소 */
@Repository
public class FileAttachmentRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public FileAttachmentRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 삭제되지 않은 파일 첨부 단건 조회 */
  public Optional<FileAttachment> findById(UUID fileAttachmentId) {
    QFileAttachment fileAttachment = QFileAttachment.fileAttachment;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(fileAttachment)
            .where(fileAttachment.id.eq(fileAttachmentId), fileAttachment.deleted.isFalse())
            .fetchOne());
  }

  /** 삭제되지 않은 대상별 파일 첨부 목록 조회 */
  public List<FileAttachment> findAllByTarget(FileTargetType targetType, UUID targetId) {
    QFileAttachment fileAttachment = QFileAttachment.fileAttachment;

    return queryFactory
        .selectFrom(fileAttachment)
        .where(
            fileAttachment.targetType.eq(targetType),
            fileAttachment.targetId.eq(targetId),
            fileAttachment.deleted.isFalse())
        .orderBy(fileAttachment.sortOrder.asc(), fileAttachment.createdAt.asc())
        .fetch();
  }
}
