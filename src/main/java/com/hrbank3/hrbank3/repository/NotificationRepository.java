package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  void deleteByDepartmentId(Long departmentId);
}
