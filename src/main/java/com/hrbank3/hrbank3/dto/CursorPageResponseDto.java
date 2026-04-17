package com.hrbank3.hrbank3.dto;

import java.util.List;

public record CursorPageResponseDto<T>(
    List<T> content,      // 실제 데이터 목록
    String nextCursor,      // 다음 페이지 시작 기준 ID
    Long nextIdAfter,     // 다음 페이지 시작 ID
    int size,             // 현재 페이지 데이터 개수
    long totalElements,   // 전체 데이터 개수
    boolean hasNext       // 다음 페이지 존재 여부
) {

}