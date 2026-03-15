# 后端Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制pom文件
COPY pom.xml .
COPY emotion-hub-common/pom.xml emotion-hub-common/
COPY emotion-hub-model/pom.xml emotion-hub-model/
COPY emotion-hub-dao/pom.xml emotion-hub-dao/
COPY emotion-hub-service/pom.xml emotion-hub-service/
COPY emotion-hub-web/pom.xml emotion-hub-web/

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源代码
COPY emotion-hub-common emotion-hub-common
COPY emotion-hub-model emotion-hub-model
COPY emotion-hub-dao emotion-hub-dao
COPY emotion-hub-service emotion-hub-service
COPY emotion-hub-web emotion-hub-web

# 构建项目
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装curl用于健康检查
RUN apk add --no-cache curl

# 从构建阶段复制jar文件
COPY --from=builder /app/emotion-hub-web/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log/emotion-hub

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]