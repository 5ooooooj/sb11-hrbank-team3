package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.EmployeeAuditHistory;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeAuditHistoryRepository extends JpaRepository<EmployeeAuditHistory, Long> {

    // 특정 시간 이후 생성된 수정 이력 건수 - 대시보드에서 사용
    long countByCreatedAtAfter(Instant createdAt);
}