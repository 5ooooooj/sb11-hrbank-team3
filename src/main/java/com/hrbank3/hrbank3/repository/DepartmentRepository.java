package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>,
        DepartmentRepositoryCustom { // 추가

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}