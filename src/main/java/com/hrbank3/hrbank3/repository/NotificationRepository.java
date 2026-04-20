package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.Notification;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
