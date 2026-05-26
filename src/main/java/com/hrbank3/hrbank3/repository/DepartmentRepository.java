package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.dto.dashboard.DepartmentDistributionDto;
import com.hrbank3.hrbank3.entity.Department;
import com.hrbank3.hrbank3.entity.enums.EmployeeStatus;
import com.hrbank3.hrbank3.repository.custom.DepartmentRepositoryCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>,
    DepartmentRepositoryCustom {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);

  // 부서별 직원 분포 - 직원 수 기준 내림차순
  @Query(
      "SELECT new com.hrbank3.hrbank3.dto.dashboard.DepartmentDistributionDto(d.id, d.name, COUNT(e)) "
          +
          "FROM Department d LEFT JOIN Employee e ON e.department = d AND e.status != :excluded " +
          "GROUP BY d.id, d.name ORDER BY COUNT(e) DESC")
  List<DepartmentDistributionDto> findAllWithEmployeeCount(
      @Param("excluded") EmployeeStatus excluded
  );
}