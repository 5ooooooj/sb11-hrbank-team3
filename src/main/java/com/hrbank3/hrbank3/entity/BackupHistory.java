package com.hrbank3.hrbank3.entity;

import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "backup_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BackupHistory {

  @Id
  @Column
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String worker;

  @Column(nullable = false)
  private Instant startedAt;

  @Column
  private Instant endedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BackupStatus status;

  // OneToOne은 FileMetadata 하나에 BackupHistory 하나만 연결된다는 의미
  // 같은 csv 파일을 여러 백업 이력이 공유하는 시나리오 없음 -> 1:1 매핑으로 재수정
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id", unique = true)
  private FileMetadata file;

  // 정적 팩토리 메서드 방식
  public static BackupHistory ofInProgress(String worker) {
    BackupHistory history = new BackupHistory();
    history.worker = worker;
    history.startedAt = Instant.now();
    history.status = BackupStatus.IN_PROGRESS;
    return history;
  }

  // 스킵할땐 시작시간=끝시간=현재시각
  public static BackupHistory ofSkipped(String worker) {
    BackupHistory history = new BackupHistory();
    history.worker = worker;
    history.startedAt = Instant.now();
    history.endedAt = Instant.now();
    history.status = BackupStatus.SKIPPED;
    return history;
  }

  public void updateComplete(FileMetadata file) {
    this.status = BackupStatus.COMPLETED;
    this.endedAt = Instant.now();
    this.file = file;
  }

  public void updateFail(FileMetadata file) {
    this.status = BackupStatus.FAILED;
    this.endedAt = Instant.now();
    this.file = file;
  }
}
