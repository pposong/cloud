# --platform=linux/amd64 → EC2 아키텍처에 맞는 베이스 이미지를 명시적으로 지정
FROM --platform=linux/arm64 amazoncorretto:17-al2023-headless
WORKDIR /app
COPY build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
