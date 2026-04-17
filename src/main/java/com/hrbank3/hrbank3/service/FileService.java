package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.file.FileDownloadDto;
import com.hrbank3.hrbank3.entity.FileMetadata;
import com.hrbank3.hrbank3.repository.FileMetadataRepository;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

  private final FileMetadataRepository fileMetadataRepository;

  // 허용할 확장자 리스트
  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "pdf");
  // 최대 용량 제한 (10MB)
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

  // 환경변수 처리를 해뒀던 yaml의 값을 가져옴
  @Value("${file.upload.path}")
  private String uploadPath;

  @Transactional
  public Long uploadFile(MultipartFile multipartFile) {
    // 파일 존재 여부 검증
    if(multipartFile == null || multipartFile.isEmpty()) {
      throw new IllegalArgumentException("업로드된 파일이 없습니다.");
    }
    // 용량 검증
    if(multipartFile.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("파일 용량은 10MB를 초과할 수 없습니다.");
    }

    // 확장자 검증
    String extensionName = multipartFile.getOriginalFilename();
    if(extensionName == null || !extensionName.contains(".")) {
      throw new IllegalArgumentException("올바르지 않은 파일명입니다.");
    }
    String ext = extensionName.substring(extensionName.lastIndexOf(".") + 1).toLowerCase();
    if(!ALLOWED_EXTENSIONS.contains(ext)) {
      throw new IllegalArgumentException("허용되지 않는 파일 형식입니다." + ext);
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

    // 로컬 디스크에 실제 파일 저장
    File localDisc = new File(storagePath);
    try {
      // transferTo() -> 파일 저장 과정에서 발생할 수 있는 I/O관련 예외를 자동으로 처리
      multipartFile.transferTo(localDisc);
    } catch (IOException e) {
      throw new RuntimeException("파일 저장 중 시스템 오류가 발생했습니다.", e);
    }

    // 응답 ID 반환
    return savedFile.getId();
  }

  @Transactional(readOnly = true)
  public FileDownloadDto downloadFile(Long id) {
    // id를 통해 DB에서 메타 정보 조회
    FileMetadata fileMetadata = fileMetadataRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 ID의 파일 메타데이터를 찾을 수 없습니다."));

    try {
      // 메타 정보에 기록된 경로를 통해 로컬 파일에 접근
      Path filePath = Paths.get(fileMetadata.getStoragePath());
      Resource resource = new UrlResource(filePath.toUri());

      // 물리적 파일이 디스크에서 삭제되어있는지 확인
      if(!resource.exists() || !resource.isReadable()) {
        throw new IllegalArgumentException("파일이 존재하지 않거나 읽을 수 없습니다.");
      }

      // 컨트롤러에 DTO로 반환
      return new FileDownloadDto(
          resource,
          fileMetadata.getOriginalName(),
          fileMetadata.getContentType()
      );
    } catch (MalformedURLException e) {
      throw new RuntimeException("파일 경로를 읽는 도중 시스템 오류가 발생했습니다.", e);
    }
  }
}