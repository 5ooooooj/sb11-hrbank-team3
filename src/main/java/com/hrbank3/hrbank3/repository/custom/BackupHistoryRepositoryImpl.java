package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.entity.BackupStatus;
import com.hrbank3.hrbank3.entity.QBackupHistory;
import com.hrbank3.hrbank3.repository.condition.BackupHistorySortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import com.hrbank3.hrbank3.entity.BackupHistory;
import com.hrbank3.hrbank3.repository.condition.BackupHistorySearchCondition;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class BackupHistoryRepositoryImpl implements BackupHistoryRepositoryCustom{
  private final JPAQueryFactory queryFactory;
  private final QBackupHistory backupHistory = QBackupHistory.backupHistory;

  // 프로로타입 기준으로 페이지 사이즈 10으로 상수 고정
  private static final int PAGE_SIZE = 10;

  @Override
  public List<BackupHistory> findAllByCondition(BackupHistorySearchCondition condition) {
    return queryFactory
        .selectFrom(backupHistory)
        //where()에 여러 조건을 넣으면 QueryDSL이 자동으로 AND로 연결하고, 조건메서드가 null을 반환하면 조건 무시함
        .where(
            workerContains(condition.getWorker()),
            startedAtBetween(condition.getStartedAtFrom(), condition.getStartedAtTo()),
            statusEq(condition.getStatus()),
            cursorCondition(condition.getLastId(), condition.getSortType())
        )
        .orderBy(resolveOrderBy(condition.getSortType()))
        .limit(PAGE_SIZE)
        .fetch();

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

  // 어디서부터 가져올지 정하는 메소드
  // gt = great than, 오름차순이면 마지막 id 이후 데이터 가져와야함 -> id > lastId
  // lt = less than, 내림차순이면 마지막 id 이전 데이터 가져와야함 -> id < lastId
  private BooleanExpression cursorCondition(Long lastId, BackupHistorySortType sortType) {
    if (lastId == null) return null;

    return switch (sortType) {
      case STARTED_AT_ASC, ENDED_AT_ASC, STATUS_ASC -> backupHistory.id.gt(lastId);
      case STARTED_AT_DESC, ENDED_AT_DESC, STATUS_DESC -> backupHistory.id.lt(lastId);
    };
  }

  // 어떤 순서로 정렬할지 정하는 메서드
  private OrderSpecifier<?> resolveOrderBy(BackupHistorySortType sortType) {
    // 기본값
    if (sortType == null) return backupHistory.startedAt.desc();

    return switch (sortType) {
      case STARTED_AT_ASC -> backupHistory.startedAt.asc();
      case STARTED_AT_DESC -> backupHistory.startedAt.desc();
      case ENDED_AT_ASC -> backupHistory.endedAt.asc().nullsLast();
      case ENDED_AT_DESC -> backupHistory.endedAt.desc().nullsLast();
      case STATUS_ASC -> backupHistory.status.asc();
      case STATUS_DESC -> backupHistory.status.desc();
    };

  }


}
