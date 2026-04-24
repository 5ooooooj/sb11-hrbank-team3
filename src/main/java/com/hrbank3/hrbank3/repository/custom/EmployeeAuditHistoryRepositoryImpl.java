package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.dto.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.audit_history.ChangeLogDto;
import com.hrbank3.hrbank3.entity.QEmployeeAuditHistory;
import com.hrbank3.hrbank3.entity.enums.AuditType;
import com.hrbank3.hrbank3.repository.condition.ChangeLogSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
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
  public CursorPageResponseDto<ChangeLogDto> findAllWithCursor(ChangeLogSearchCondition condition) {
    BooleanBuilder builder = new BooleanBuilder();

    // 필터링 조건 추가
    builder.and(employeeNumberContains(condition.employeeNumber()))
        .and(typeEq(condition.type()))
        .and(memoContains(condition.memo()))
        .and(ipAddressContains(condition.ipAddress()))
        .and(createdAtBetween(condition.atFrom(), condition.atTo()));

    // 전체 데이터 개수 조회
    long totalElements = Optional.ofNullable(
        queryFactory.select(history.count()).from(history).where(builder).fetchOne()
    ).orElse(0L);

    // 커서 페이징 조건 추가
    builder.and(
        cursorCondition(condition.cursor(), condition.idAfter(), condition.sortField(),
            condition.sortDirection()));

    // 데이터 조회
    List<ChangeLogDto> results = queryFactory
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
        .limit(condition.size() + 1) // 다음 페이지가 있는지 확인하기 위해 1개 더 조회
        .fetch();

    // 다음 페이지 존재 여부 확인
    boolean hasNext = results.size() > condition.size();
    if (hasNext) {
      results = results.subList(0, condition.size());
    }

    // 다음 페이지를 위한 커서 값 생성
    String nextCursor = null;
    Long nextIdAfter = null;
    if (hasNext) {
      ChangeLogDto lastItem = results.get(results.size() - 1);
      nextIdAfter = lastItem.id();
      // 시간/IP주소 정렬 기준에 따라 다음 커서값 세팅
      nextCursor = "ipAddress".equals(condition.sortField())
          ? lastItem.ipAddress()
          : lastItem.at().toString();
    }

    return new CursorPageResponseDto<>(results, nextCursor, nextIdAfter, results.size(),
        totalElements, hasNext);
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

  private BooleanExpression createdAtBetween(java.time.ZonedDateTime from,
      java.time.ZonedDateTime to) {
    if (from == null && to == null) {
      return null;
    }
    Instant fromInstant = from != null ? from.toInstant() : null;
    Instant toInstant = to != null ? to.plusDays(1).minusNanos(1).toInstant() : null;

    if (fromInstant == null) {
      return history.createdAt.loe(toInstant);
    }
    if (toInstant == null) {
      return history.createdAt.goe(fromInstant);
    }
    return history.createdAt.between(fromInstant, toInstant);
  }

  /*커서 및 정렬 로직*/

  // 동적 커서 비교 로직
  private BooleanExpression cursorCondition(String cursor, Long lastId, String sortField,
      String sortDirection) {

    if (!StringUtils.hasText(cursor) || lastId == null) {
      return null;
    }

    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

    if ("ipAddress".equals(sortField)) {
      // IP 정렬일 때 커서 처리
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
  private OrderSpecifier<?>[] resolveOrderSpecifiers(String sortField, String sortDirection) {
    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

    if ("ipAddress".equals(sortField)) {
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