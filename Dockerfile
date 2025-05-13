FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# GHCR 인증용 빌드 아규먼트 (보안 경고 방지를 위해 ENV로는 안 씀)
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

# settings.xml 복사
COPY .m2/settings.xml /root/.m2/settings.xml

COPY . .

# Maven 테스트 생략하고 빌드 (환경변수는 RUN에서 직접 주입)
RUN GITHUB_ACTOR=${GITHUB_ACTOR} GITHUB_TOKEN=${GITHUB_TOKEN} ./mvnw clean install -Dmaven.test.skip=true

# 런타임 이미지
FROM eclipse-temurin:21-jdk
COPY --from=builder /app/target/*.jar /app/app.jar
ENTRYPOINT ["top", "-b"]