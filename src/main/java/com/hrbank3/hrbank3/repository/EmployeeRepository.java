package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.dto.dashboard.PositionDistributionDto;
import com.hrbank3.hrbank3.entity.Employee;
import com.hrbank3.hrbank3.entity.enums.EmployeeStatus;
import com.hrbank3.hrbank3.repository.custom.EmployeeRepositoryCustom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<Employee, Long>,
    EmployeeRepositoryCustom {

  // 기본적인 CRUD는 JpaRepository가 기본 제공함
  boolean existsByEmail(String email);

  Optional<Employee> findByEmployeeNumber(String employeeNumber);

  // 특정 시간 이후 변경된 직원 존재 여부
  boolean existsByUpdatedAtAfter(Instant lastBackupTime);

  // 퇴사자 제외한 총 직원 수
  long countByStatusNot(EmployeeStatus status);

  // 이번달 입사자 수
  long countByHireDateGreaterThanEqualAndStatus(LocalDate hireDate, EmployeeStatus status);

  // 월별 직원수 추이 - 해당 월 말일 기준 퇴사자 제외 직원 수
  long countByHireDateLessThanEqualAndStatusNot(LocalDate hireDate, EmployeeStatus status);

  // 직무별 직원 분포
  @Query(
      "SELECT new com.hrbank3.hrbank3.dto.dashboard.PositionDistributionDto(e.position, COUNT(e)) "
          +
          "FROM Employee e WHERE e.status != 'RESIGNED' GROUP BY e.position ORDER BY COUNT(e) DESC")
  List<PositionDistributionDto> findPositionDistribution();

  // 백업 히스토리서비스에서 csv 청크 조회시 n+1 발생 -> 프로필 이미지로 직원 찾는 메서드 추가
  // 레프트 조인으로 없어도 전체 직원 조회, 정렬은 서비스에서
  @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.profileImage")
  Slice<Employee> findAllWithProfileImage(Pageable pageable);

}