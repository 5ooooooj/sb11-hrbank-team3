package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.dto.audit_history.ChangeLogDto;
import com.hrbank3.hrbank3.entity.QEmployeeAuditHistory;
import com.hrbank3.hrbank3.entity.enums.AuditSortField;
import com.hrbank3.hrbank3.entity.enums.AuditType;
import com.hrbank3.hrbank3.repository.condition.ChangeLogSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class EmployeeAuditHistoryRepositoryImpl implements EmployeeAuditHistoryRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QEmployeeAuditHistory history = QEmployeeAuditHistory.employeeAuditHistory;

  @Override
  public List<ChangeLogDto> findChangeLogs(ChangeLogSearchCondition condition) {
    BooleanBuilder builder = new BooleanBuilder();

    // 필터링 및 커서 조건 추가
    builder.and(employeeNumberContains(condition.employeeNumber()))
        .and(typeEq(condition.type()))
        .and(memoContains(condition.memo()))
        .and(ipAddressContains(condition.ipAddress()))
        .and(createdAtBetween(condition.atFrom(), condition.atTo()))
        .and(cursorCondition(condition.cursor(), condition.idAfter(), condition.sortField(),
            condition.sortDirection()));

    return queryFactory
        .select(Projections.constructor(ChangeLogDto.class,
            history.id,
            history.auditType,
            history.targetEmployeeNo,
            history.memo,
            history.ipAddress,
            history.createdAt
        ))
        .from(history)
        .where(builder)
        .orderBy(resolveOrderSpecifiers(condition.sortField(), condition.sortDirection()))
        .limit(condition.size() + 1) // Service에서 hasNext를 확인할 수 있도록 1개 더 조회
        .fetch();
  }

  // count 쿼리
  @Override
  public long countChangeLogs(ChangeLogSearchCondition condition) {
    BooleanBuilder builder = new BooleanBuilder();

    builder.and(employeeNumberContains(condition.employeeNumber()))
        .and(typeEq(condition.type()))
        .and(memoContains(condition.memo()))
        .and(ipAddressContains(condition.ipAddress()))
        .and(createdAtBetween(condition.atFrom(), condition.atTo()));

    return Optional.ofNullable(
        queryFactory.select(history.count()).from(history).where(builder).fetchOne()
    ).orElse(0L);
  }

  /*필터링 조건 메서드*/

  private BooleanExpression employeeNumberContains(String employeeNumber) {
    return StringUtils.hasText(employeeNumber) ? history.targetEmployeeNo.contains(employeeNumber)
        : null;
  }

  private BooleanExpression typeEq(AuditType type) {
    return type != null ? history.auditType.eq(type) : null;
  }

  private BooleanExpression memoContains(String memo) {
    return StringUtils.hasText(memo) ? history.memo.contains(memo) : null;
  }

  private BooleanExpression ipAddressContains(String ipAddress) {
    return StringUtils.hasText(ipAddress) ? history.ipAddress.contains(ipAddress) : null;
  }

  private BooleanExpression createdAtBetween(Instant from, Instant to) {
    if (from == null && to == null) {
      return null;
    }

    Instant endOfTo =
        to != null ? to.plus(1, ChronoUnit.DAYS).minusNanos(1) : null;

    if (from == null) {
      return history.createdAt.loe(endOfTo);
    }
    if (endOfTo == null) {
      return history.createdAt.goe(from);
    }
    return history.createdAt.between(from, endOfTo);
  }

  /*커서 및 정렬 로직*/

  // 동적 커서 비교 로직
  private BooleanExpression cursorCondition(String cursor, Long lastId, AuditSortField sortField,
      String sortDirection) {

    if (!StringUtils.hasText(cursor) || lastId == null) {
      return null;
    }

    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

    if (sortField == AuditSortField.IP_ADDRESS) {
      if (isAsc) {
        return history.ipAddress.gt(cursor)
            .or(history.ipAddress.eq(cursor).and(history.id.gt(lastId)));
      } else {
        return history.ipAddress.lt(cursor)
            .or(history.ipAddress.eq(cursor).and(history.id.lt(lastId)));
      }
    } else {
      // at 정렬일 때 커서 처리
      Instant cursorInstant = Instant.parse(cursor);
      if (isAsc) {
        return history.createdAt.gt(cursorInstant)
            .or(history.createdAt.eq(cursorInstant).and(history.id.gt(lastId)));
      } else {
        return history.createdAt.lt(cursorInstant)
            .or(history.createdAt.eq(cursorInstant).and(history.id.lt(lastId)));
      }
    }
  }

  // 동적 정렬 로직
  private OrderSpecifier<?>[] resolveOrderSpecifiers(AuditSortField sortField,
      String sortDirection) {
    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

    if (sortField == AuditSortField.IP_ADDRESS) {
      return new OrderSpecifier[]{
          isAsc ? history.ipAddress.asc() : history.ipAddress.desc(),
          isAsc ? history.id.asc() : history.id.desc()
      };
    } else {
      return new OrderSpecifier[]{
          isAsc ? history.createdAt.asc() : history.createdAt.desc(),
          isAsc ? history.id.asc() : history.id.desc()
      };
    }
  }
}