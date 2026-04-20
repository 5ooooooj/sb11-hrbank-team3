package com.hrbank3.hrbank3.entity;

import com.hrbank3.hrbank3.entity.enums.EmployeeStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부에서 호출 방지
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "employees")
public class Employee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // -> DB에게 ID 자동생성 맡김
  private Long id;

  @Column(name = "employee_number", nullable = false, unique = true, length = 50)
  private String employeeNumber;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "department_id", nullable = false)
  private Long departmentId;

  @Column(nullable = false, length = 50)
  private String position;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private EmployeeStatus status;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
  @JoinColumn(name = "profile_image_id")
  private FileMetadata profileImage;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  // DB에 INSERT 되기 직전에 실행, 저장될때 한번만 실행, 자동으로 값을 넣어주는 항목만
  protected void onCreate() {
    // 같은 패키지 or 상속 클래스에서만 호출 가능, 외부 호출 방지
    this.status = EmployeeStatus.ACTIVE;
    this.employeeNumber = generateEmployeeNumber();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  private String generateEmployeeNumber() {
    String year = String.valueOf(LocalDate.now().getYear());
    String random = String.format("%014d", (long) (Math.random() * 100_000_000_000_000L));
    return "EMP-" + year + "-" + random;
  }

  public static Employee create(String name, String email, Long departmentId,
      String position, LocalDate hireDate, FileMetadata profileImage) {
    Employee employee = new Employee();
    employee.name = name;
    employee.email = email;
    employee.departmentId = departmentId;
    employee.position = position;
    employee.hireDate = hireDate;
    employee.profileImage = profileImage;
    return employee;
  }

  public void update(String name, String email, Long departmentId,
      String position, LocalDate hireDate, EmployeeStatus status) {
    this.name = name;
    this.email = email;
    this.departmentId = departmentId;
    this.position = position;
    this.hireDate = hireDate;
    this.status = status;
  }

  public void updateProfileImage(FileMetadata newProfileImage) {
    this.profileImage = newProfileImage;
  }
}
