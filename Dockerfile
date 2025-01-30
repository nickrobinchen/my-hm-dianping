# 使用官方的 OpenJDK 作为基础镜像
FROM amazoncorretto:17

# 复制构建的 JAR 文件到容器中的 /app 目录
COPY target/hm-dianping-0.0.1-SNAPSHOT.jar /app/app.jar

# 暴露 Spring Boot 应用的端口
EXPOSE 8081

# 设置容器启动时运行 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]