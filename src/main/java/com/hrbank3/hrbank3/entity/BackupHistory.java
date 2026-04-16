package com.hrbank3.hrbank3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id")
  private FileMetadata file;

  public BackupHistory(String worker) {
    this.worker = worker;
    this.startedAt = Instant.now();
    this.status = BackupStatus.IN_PROGRESS;
  }

  public void updateComplete(FileMetadata file) {
    this.status = BackupStatus.COMPLETE;
    this.endedAt = Instant.now();
    this.file = file;
  }

  public void updateFail(FileMetadata file) {
    this.status = BackupStatus.FAILED;
    this.endedAt = Instant.now();
    this.file = file;
  }
}
