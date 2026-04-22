package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.BackupHistory;
import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import com.hrbank3.hrbank3.repository.custom.BackupHistoryRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository와 BackupHistoryRepositoryCustom 둘 다 상속해서 기본 CRUD와 QueryDSL 커스텀 쿼리 같이 쓰기
public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long>,
    BackupHistoryRepositoryCustom {

  // 백업 필요 여부를 판단하기 위한 메소드
  // 가장 최근 완료된 배치 작업 시간 이후 직원 데이터가 변경되었는지 조회
  Optional<BackupHistory> findTopByStatusOrderByStartedAtDesc(BackupStatus status);

  // 현재 진행중인 백업이 있나 확인
  boolean existsByStatus(BackupStatus status);
}
