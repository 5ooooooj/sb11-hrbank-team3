package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.entity.BackupStatus;
import com.hrbank3.hrbank3.entity.QBackupHistory;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import com.hrbank3.hrbank3.entity.BackupHistory;
import com.hrbank3.hrbank3.repository.condition.BackupHistorySearchCondition;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class BackupHistoryRepositoryImpl implements BackupHistoryRepositoryCustom{
  private final JPAQueryFactory queryFactory;
  private final QBackupHistory backupHistory = QBackupHistory.backupHistory;

  @Override
  public List<BackupHistory> findAllByCondition(BackupHistorySearchCondition condition) {
    return queryFactory
        .selectFrom(backupHistory)
        .where(
            workerContains(condition.getWorker()),
            startedAtBetween(condition.getStartedAtFrom(), condition.getStartedAtTo()),
            statusEq(condition.getStatus())
        )
        .fetch();
    //where()에 여러 조건을 넣으면 QueryDSL이 자동으로 AND로 연결하고, 조건메서드가 null을 반환하면 조건 무시함
  }

  // BooleanExpression은 sql의 where 조건절을 객체로 표현한 것
  // WHERE worker LIKE '%문자열%' 과 같은 기능
  // 작업자 부분 일치 조회를 위한 메소드
  private BooleanExpression workerContains(String worker) {
    return StringUtils.hasText(worker)
        ? backupHistory.worker.contains(worker)
        : null;
  }

  // 시작시간 범위 일치 조회를 위한 메소드
  private BooleanExpression startedAtBetween(ZonedDateTime from, ZonedDateTime to) {
    if (from == null && to == null) return null;

    // 조회를 위한 Instant 변환
    Instant fromInstant = from != null ? from.toInstant() : null;
    Instant toInstant = to != null ? to.toInstant() : null;

    if (fromInstant == null) return backupHistory.startedAt.loe(toInstant);
    if (toInstant == null) return backupHistory.startedAt.goe(fromInstant);
    return backupHistory.startedAt.between(fromInstant, toInstant);
  }

  // 상태 완전 일치 조회를 위한 메소드
  private BooleanExpression statusEq(BackupStatus status) {
    return status != null
        ? backupHistory.status.eq(status)
        : null;
  }

}
