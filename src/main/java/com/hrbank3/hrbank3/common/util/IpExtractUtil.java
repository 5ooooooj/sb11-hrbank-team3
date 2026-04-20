package com.hrbank3.hrbank3.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IpExtractUtil {

  // Request 객체를 직접 넘겨받을 때 사용하는 메서드
  public static String getClientIp(HttpServletRequest request) {
    if (request == null) {
      return "Unknown IP";
    }
    String ip = request.getHeader("X-Forwarded-For");

    if (ip == null || ip.isBlank()) {
      return request.getRemoteAddr();
    }

    // 프록시 체인 환경에서 최초 클라이언트 IP만 추출
    return ip.split(",")[0].trim();
  }


  // Request 파라미터 없이 현재 스레드에서 추출하는 메서드
  public static String getClientIpFromContext() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

    if (!(attributes instanceof ServletRequestAttributes)) {
      return "System/Batch";
    }

    HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
    return getClientIp(request);
  }
}