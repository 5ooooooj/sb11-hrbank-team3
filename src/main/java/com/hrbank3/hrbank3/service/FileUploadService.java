package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.entity.FileMetadata;
import com.hrbank3.hrbank3.repository.FileMetadataRepository;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploadService {

  private final FileMetadataRepository fileMetadataRepository;

  // 환경변수 처리를 해뒀던 yaml의 값을 가져옴
  @Value("${file.upload.path}")
  private String uploadPath;

  @Transactional
  public Long uploadFile(MultipartFile multipartFile) throws IOException {
    // 파일 존재 여부 검증
    if(multipartFile == null || multipartFile.isEmpty()) {
      throw new IllegalArgumentException("업로드된 파일이 없습니다.");
    }

    // 저장 디렉토리 확인 및 생성 (없으면 자동으로 생성)
    File uploadDir = new File(uploadPath);
    if(!uploadDir.exists()) {
      uploadDir.mkdirs();
    }

    // 원본 파일명 및 컨텐츠 타입, 크기 추출
    String originalName = multipartFile.getOriginalFilename();
    String contentType = multipartFile.getContentType();
    Long fileSize = multipartFile.getSize();

    // 저장 파일명 생성(UUID - originalName) 및 저장경로 세팅
    String storedName = UUID.randomUUID().toString() + "-" + originalName;
    String storagePath = uploadPath + storedName;

    // 로컬 디스크에 실제 파일 저장
    File localDisc = new File(storagePath);
    // transferTo() -> 파일 저장 과정에서 발생할 수 있는 I/O관련 예외를 자동으로 처리
    multipartFile.transferTo(localDisc);

    // DB에 저장할 메타데이터 Entity 생성
    FileMetadata fileMetadata = new FileMetadata(
        originalName,
        storedName,
        contentType,
        fileSize,
        storagePath
    );

    // Entity 저장
    FileMetadata savedFile = fileMetadataRepository.save(fileMetadata);

    // 응답 ID 반환
    return savedFile.getId();
  }
}
