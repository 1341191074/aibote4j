FROM openjdk:21-slim

# 设置标签
LABEL maintainer="AIBoTe Project"
LABEL version="1.0"
LABEL description="AIBoTe4J - Cross-platform RPA Framework"

# 工作目录
WORKDIR /app

# 复制项目文件
COPY . .

# 安装Maven并编译项目
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean install -DskipTests && \
    rm -rf ~/.m2 && \
    apt-get clean

# 创建应用目录
RUN mkdir -p /app/logs /app/config

# 暴露端口
EXPOSE 8080 8081 9000

# 设置JVM参数
ENV JVM_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 运行应用
CMD ["sh", "-c", "java ${JVM_OPTS} -jar sdk-server/target/aibote-server-1.0-jar-with-dependencies.jar"]

