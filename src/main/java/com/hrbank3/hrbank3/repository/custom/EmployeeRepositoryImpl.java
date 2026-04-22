package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.entity.Employee;
import com.hrbank3.hrbank3.entity.enums.EmployeeStatus;
import com.hrbank3.hrbank3.entity.QEmployee;
import com.hrbank3.hrbank3.repository.condition.EmployeeSearchCondition;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QEmployee employee = QEmployee.employee;

  @Override
  public List<Employee> findAllByCondition(EmployeeSearchCondition condition) {
    return queryFactory
        .selectFrom(employee)
        .leftJoin(employee.department).fetchJoin()
        .leftJoin(employee.profileImage).fetchJoin()
        .where(
            nameOrEmailContains(condition.nameOrEmail()),
            employeeNumberContains(condition.employeeNumber()),
            departmentNameContains(condition.departmentName()),
            positionContains(condition.position()),
            hireDateBetween(condition.hireDateFrom(), condition.hireDateTo()),
            statusEq(condition.status()),
            cursorCondition(condition.idAfter())
        )
        .orderBy(resolveOrderBy(condition.sortField(), condition.sortDirection()))
        .limit(condition.size() + 1L)
        .fetch();
  }


  @Override
  public long countByCondition(EmployeeSearchCondition condition) {
    // DB에서 COUNT(*)쿼리로 개수만 조회
    // .selectFrom(employee)로 전부 가져와서 .fetch().size() 사용시
    // 전체 데이터를 메모리에 올리게 되어 성능 문제가 생길 수 있음
    Long count = queryFactory
        .select(employee.count())
        .from(employee)
        .leftJoin(employee.department) // fetch 없이 join만 추가
        .where(
            nameOrEmailContains(condition.nameOrEmail()),
            employeeNumberContains(condition.employeeNumber()),
            departmentNameContains(condition.departmentName()),
            positionContains(condition.position()),
            hireDateBetween(condition.hireDateFrom(), condition.hireDateTo()),
            statusEq(condition.status())
        ) // 커서 조건 넣으면 전체 수가 아니라 커서 이후의 수만 세게 됨
        .fetchOne();
    // .fetchCount()를 쓰려 하였으나, QueryDSL 5.0 이후로 더 이상 사용되지 않아서
    return count != null ? count : 0L;
  }

  // BooleanExpression은 SQL의 WHERE절을 자바 객체로 표현한 것
  // null이거나 " " 반환시 쿼리DSL이 자동으로 조건 무시
  private BooleanExpression nameOrEmailContains(String nameOrEmail) {
    if (!StringUtils.hasText(nameOrEmail)) {
      return null;
    }
    return employee.name.contains(nameOrEmail)
        .or(employee.email.contains(nameOrEmail));
  }

  // 삼항연산자
  // 조건 ? 참 : 거짓
  private BooleanExpression employeeNumberContains(String employeeNumber) {
    return StringUtils.hasText(employeeNumber)
        ? employee.employeeNumber.contains(employeeNumber)
        : null;
  }

  // 부서명 부분 일치 부분은 추후 부서 연동 후 구현
  private BooleanExpression departmentNameContains(String departmentName) {
    if (!StringUtils.hasText(departmentName)) {
      return null;
    }
    return employee.department.name.contains(departmentName);
  }

  private BooleanExpression positionContains(String position) {
    return StringUtils.hasText(position)
        ? employee.position.contains(position)
        : null;
  }

  // 명세서 hireDateFrom: string($date)
  private BooleanExpression hireDateBetween(LocalDate from, LocalDate to) {
    if (from == null && to == null) {
      return null;
    }
    if (from == null) {
      // less or equal, <= 와 같음
      return employee.hireDate.loe(to);
    }
    if (to == null) {
      // greater or equal, >= 와 같음
      return employee.hireDate.goe(from);
    }
    return employee.hireDate.between(from, to);
  }

  private BooleanExpression statusEq(EmployeeStatus status) {
    return status != null ? employee.status.eq(status) : null;
  }

  private BooleanExpression cursorCondition(Long idAfter) {
    // greater then, > 와 같음
    // idAfter가 있으면 id > idAfter, 없으면 처음부터 가져옴
    return idAfter != null ? employee.id.gt(idAfter) : null;
  }

  private OrderSpecifier<?> resolveOrderBy(String sortField, String sortDirection) {
    // 대소문자 구분 없이 비교, desc 와 DESC는 같음
    boolean isAsc = !"desc".equalsIgnoreCase(sortDirection);
    if ("employeeNumber".equals(sortField)) {
      return isAsc ? employee.employeeNumber.asc() : employee.employeeNumber.desc();
    }
    if ("hireDate".equals(sortField)) {
      return isAsc ? employee.hireDate.asc() : employee.hireDate.desc();
    }
    return isAsc ? employee.name.asc() : employee.name.desc();
  }
}
