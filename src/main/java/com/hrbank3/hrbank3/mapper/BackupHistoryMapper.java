package com.hrbank3.hrbank3.mapper;

import com.hrbank3.hrbank3.dto.backup_history.BackupHistoryDto;
import com.hrbank3.hrbank3.entity.BackupHistory;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BackupHistoryMapper {

  // 서울 기준시간 상수로 정의
  ZoneId KST = ZoneId.of("Asia/Seoul");

  @Mapping(target = "fileId", source = "file.id")
    // 객체에서 아이디만 꺼내기
  BackupHistoryDto toDto(BackupHistory entity);

  // MapStruct는 매핑시 타입이 맞지 않으면 같은 Mapper 안에 변환 메서드가 있는지 찾고 자동으로 끼워넣음 그걸 위해 메소드 정의
  default ZonedDateTime toZonedDateTime(Instant instant) {
    return instant == null ? null : instant.atZone(KST);
  }

  default Instant toInstant(ZonedDateTime zonedDateTime) {
    return zonedDateTime == null ? null : zonedDateTime.toInstant();
  }

}
