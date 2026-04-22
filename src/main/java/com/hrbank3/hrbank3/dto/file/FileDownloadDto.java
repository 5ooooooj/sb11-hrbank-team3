package com.hrbank3.hrbank3.dto.file;

import org.springframework.core.io.Resource;

public record FileDownloadDto(
    Resource resource,
    String originalName,
    String contentType
) {}
