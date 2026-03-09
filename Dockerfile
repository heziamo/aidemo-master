# 基础镜像（推荐 eclipse-temurin，更干净；或 openjdk:17-jdk-slim / 21-jdk-slim）
FROM eclipse-temurin:21-jdk-alpine

# 设置工作目录
WORKDIR /app

# 把打包好的 JAR 复制进来（注意改成你自己的 JAR 名）
COPY target/*.jar app.jar
# 如果是 Gradle：COPY build/libs/*.jar app.jar

# 暴露端口（Spring Boot 默认 8080，改成你的也行）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]