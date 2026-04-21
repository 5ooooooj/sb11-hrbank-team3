package com.hrbank3.hrbank3.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "departments")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Department {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(length = 255)
  private String description;

  @Column(nullable = false)
  private LocalDate establishedDate;

  @Column(nullable = false, length = 255)
  private String departmentEmail;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  // 생성 전용 생성자
  public Department(String name, String description, LocalDate establishedDate,
      String departmentEmail) {
    this.name = name;
    this.description = description;
    this.establishedDate = establishedDate;
    this.departmentEmail = departmentEmail;
  }

  // 수정 전용 메서드
  public void update(String name, String description, LocalDate establishedDate,
      String departmentEmail) {
    this.name = name;
    this.description = description;
    this.establishedDate = establishedDate;
    this.departmentEmail = departmentEmail;
  }
}