package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.EmployeeAuditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeAuditHistoryRepository extends JpaRepository<EmployeeAuditHistory, Long> {

}