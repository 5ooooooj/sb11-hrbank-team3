package com.hrbank.hrbank.repository;

import com.hrbank.hrbank.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>,
        DepartmentRepositoryCustom { // 추가

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}