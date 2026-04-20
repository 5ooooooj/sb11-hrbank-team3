package com.hrbank3.hrbank3.config;

import com.hrbank3.hrbank3.service.BackupHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduler {

  private final BackupHistoryService backupHistoryService;

  @Scheduled(cron = "${backup.schedule.cron}")
  public void runBackup() {
    log.info("[BackupScheduler] 배치 작업 시작");
    try {
      backupHistoryService.backup("system");
    } catch (IllegalStateException e) {
      log.warn("[BackupScheduler] 이미 진행 중인 백업이 있습니다: {}", e.getMessage());
    } catch (Exception e) {
      log.error("[BackupScheduler] 배치 작업 중 예외 발생: ", e);
    }
  }

}
