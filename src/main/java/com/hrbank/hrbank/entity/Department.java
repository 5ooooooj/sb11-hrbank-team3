package com.hrbank.hrbank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "departments") // 테이블명 지정
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id 자동 증가
    private Long id;

    @Column(nullable = false, unique = true, length = 100) // 필수, 중복 불가
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDate establishedDate; // 날짜만 (시간 없음)

    @CreatedDate
    @Column(nullable = false, updatable = false) // 한번 저장 후 수정 불가
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;


}