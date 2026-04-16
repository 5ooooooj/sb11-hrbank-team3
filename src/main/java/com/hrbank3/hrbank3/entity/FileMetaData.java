package com.hrbank3.hrbank3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FileMetaData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "original_name", nullable = false)
  private String originalName;

  @Column(name = "stored_name", nullable = false)
  private String storedName;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "size", nullable = false)
  private Long size;

  @Column(name = "storage_path", nullable = false)
  private String storagePath;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private ZonedDateTime createdAt;

  public FileMetaData(String originalName, String storedName, String contentType, Long size, String storagePath) {
    this.originalName = originalName;
    this.storedName = storedName;
    this.contentType = contentType;
    this.size = size;
    this.storagePath = storagePath;
  }
}
