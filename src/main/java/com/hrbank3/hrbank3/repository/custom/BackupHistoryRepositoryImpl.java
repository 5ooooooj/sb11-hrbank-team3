package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import com.hrbank3.hrbank3.entity.QBackupHistory;
import com.hrbank3.hrbank3.entity.enums.BackupHistorySortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import com.hrbank3.hrbank3.entity.BackupHistory;
import com.hrbank3.hrbank3.repository.condition.BackupHistorySearchCondition;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class BackupHistoryRepositoryImpl implements BackupHistoryRepositoryCustom{
  private final JPAQueryFactory queryFactory;
  private final QBackupHistory backupHistory = QBackupHistory.backupHistory;


  @Override
  public long countByCondition(BackupHistorySearchCondition condition) {
    return Optional.ofNullable(queryFactory
        .select(backupHistory.count())
        .from(backupHistory)
        .where(
            workerContains(condition.getWorker()),
            startedAtBetween(condition.getStartedAtFrom(), condition.getStartedAtTo()),
            statusEq(condition.getStatus())
        )
        .fetchOne()
    ).orElse(0L);
  }

  @Override
  public List<BackupHistory> findAllByCondition(BackupHistorySearchCondition condition) {
    return queryFactory
        .selectFrom(backupHistory)
        //where()에 여러 조건을 넣으면 QueryDSL이 자동으로 AND로 연결하고, 조건메서드가 null을 반환하면 조건 무시함
        .where(
            workerContains(condition.getWorker()),
            startedAtBetween(condition.getStartedAtFrom(), condition.getStartedAtTo()),
            statusEq(condition.getStatus()),
            cursorCondition(condition.getCursor(), condition.getLastId(), condition.getSortType())
        )
        .orderBy(resolveOrderBy(condition.getSortType()))
        .limit(condition.getPageSize()) // 서비스에서 PAGE_SIZE + 1 로 넘어옴
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
  // gt = great than, lt = less than
  private BooleanExpression cursorCondition(String cursor, Long lastId, BackupHistorySortType sortType) {
    // cursor나 lastId가 없으면 첫 페이지이므로 조건 없음
    if (cursor == null || lastId == null) return null;

    // Base64로 인코딩된 cursor를 디코딩해서 Instant로 변환
    Instant cursorInstant = Instant.parse(
        new String(Base64.getDecoder().decode(cursor))
    );

    return switch (sortType) {
      case STARTED_AT_ASC ->
          // 오름차순: 마지막 요소보다 startedAt이 큰 것을 가져옴
          // 단, startedAt이 같으면 id가 더 큰 것을 가져옴
        backupHistory.startedAt.gt(cursorInstant)
            .or(backupHistory.startedAt.eq(cursorInstant)
                .and(backupHistory.id.gt(lastId)));
      case STARTED_AT_DESC ->
          // 내림차순: 마지막 요소보다 startedAt이 더 작은 것을 가져옴
          // 단, startedAt이 같으면 id가 더 작은 것을 가져옴
        backupHistory.startedAt.lt(cursorInstant)
            .or(backupHistory.startedAt.eq(cursorInstant)
                .and(backupHistory.id.lt(lastId)));
      case ENDED_AT_ASC ->
        backupHistory.endedAt.gt(cursorInstant)
            .or(backupHistory.endedAt.eq(cursorInstant)
                .and(backupHistory.id.gt(lastId)));
      case ENDED_AT_DESC ->
        backupHistory.endedAt.lt(cursorInstant)
            .or(backupHistory.endedAt.eq(cursorInstant)
                .and(backupHistory.id.lt(lastId)));
      case STATUS_ASC -> backupHistory.id.gt(lastId);
      // status는 Instant가 아닌 Enum이라 커서 비교 불가능, id 대소비교로 대체
      case STATUS_DESC -> backupHistory.id.lt(lastId);
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
