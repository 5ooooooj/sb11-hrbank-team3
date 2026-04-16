package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.BackupHistory;
import com.hrbank3.hrbank3.repository.custom.BackupHistoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository와 BackupHistoryRepositoryCustom 둘 다 상속해서 기본 CRUD와 QueryDSL 커스텀 쿼리 같이 쓰기
public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long>,
    BackupHistoryRepositoryCustom {
}
