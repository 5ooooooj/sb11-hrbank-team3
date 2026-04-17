package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.dto.file.FileDownloadDto;
import com.hrbank3.hrbank3.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

@Tag(name = "파일 관리", description = "파일 관리 API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

  private final FileService fileService;

  @Operation(summary = "파일 다운로드")
  @GetMapping("/{id}/download")
  public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
    // Service를 호출하여 파일 리소스 및 메타데이터를 받아옴
    FileDownloadDto downloadDto = fileService.downloadFile(id);

    // 한글 파일명 깨짐 방지 URL 인코딩
    String encodedFileName = UriUtils.encode(downloadDto.originalName(), StandardCharsets.UTF_8);

    // 화면에 콘텐츠를 띄우지 않고 파일로 다운로드
    String content = "attachment; filename=\"" + encodedFileName + "\"";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, content)
        .header(HttpHeaders.CONTENT_TYPE, downloadDto.contentType())
        .body(downloadDto.resource());
  }
}
