package com.hrbank.hrbank.repository;

import com.hrbank.hrbank.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // DB 접근 클래스임을 표시
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // 이름 중복 체크용 (부서 등록/수정 시 사용)
    boolean existsByName(String name);

    // 이름으로 부서 찾기 (수정 시 중복 체크에 사용)
    boolean existsByNameAndIdNot(String name, Long id);
}