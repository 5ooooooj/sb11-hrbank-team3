# ---- Build Stage ----
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# 의존성 캐시 레이어 분리 (빌드 속도 최적화)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ---- Run Stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 저장 디렉토리
RUN mkdir -p /app/backups /app/uploads /app/logs

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]