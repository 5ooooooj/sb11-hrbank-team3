package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.dto.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.department.DepartmentDto;
import com.hrbank3.hrbank3.entity.Department;
import com.hrbank3.hrbank3.entity.QDepartment;
import com.hrbank3.hrbank3.entity.QEmployee;
import com.hrbank3.hrbank3.entity.enums.EmployeeStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DepartmentRepositoryImpl implements DepartmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponseDto<DepartmentDto> findAllWithCursor(
            String nameOrDescription,
            String sortField,       // sortBy → sortField
            String sortDirection,
            Long idAfter,           // lastId → idAfter
            int size) {

        QDepartment department = QDepartment.department;
        QEmployee employee = QEmployee.employee;
        BooleanBuilder builder = new BooleanBuilder();

        // 키워드 필터링
        if (nameOrDescription != null && !nameOrDescription.isBlank()) {
            builder.and(
                    department.name.containsIgnoreCase(nameOrDescription)
                            .or(department.description.containsIgnoreCase(nameOrDescription))
            );
        }

        // 전체 개수 조회
        long totalElements = queryFactory
                .selectFrom(department)
                .where(builder)
                .fetchCount();

        // 커서 조건 추가
        if (idAfter != null) {
            builder.and(department.id.gt(idAfter));
        }

        // 정렬 및 조회
        List<Department> results = queryFactory
                .selectFrom(department)
                .where(builder)
                .orderBy("desc".equals(sortDirection)
                        ? ("establishedDate".equals(sortField)
                        ? department.establishedDate.desc()
                        : department.name.desc())
                        : ("establishedDate".equals(sortField)
                        ? department.establishedDate.asc()
                        : department.name.asc()))
                .limit(size + 1)
                .fetch();

        // 다음 페이지 여부 확인
        boolean hasNext = results.size() > size;
        if (hasNext) {
            results = results.subList(0, size);
        }

        // employeeCount 실제 조회
        List<DepartmentDto> content = results.stream()
                .map(d -> {
                    Long count = queryFactory
                            .select(employee.count())
                            .from(employee)
                            .where(employee.department.eq(d),
                                    employee.status.ne(EmployeeStatus.RESIGNED))
                            .fetchOne();
                    return new DepartmentDto(
                            d.getId(),
                            d.getName(),
                            d.getDescription(),
                            d.getEstablishedDate(),
                            d.getCreatedAt(),
                            d.getUpdatedAt(),
                            count != null ? count : 0L
                    );
                })
                .collect(Collectors.toList());

        String nextCursor = hasNext ? String.valueOf(results.get(results.size() - 1).getId()) : null;
        Long nextIdAfter = hasNext ? results.get(results.size() - 1).getId() : null;

        return new CursorPageResponseDto<>(
                content,
                nextCursor,
                nextIdAfter,
                content.size(),
                totalElements,
                hasNext
        );
    }
}