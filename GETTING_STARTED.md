# 快速开始 - aibote4j

欢迎使用 aibote4j！本框架是一个基于 Java 和 Netty 的分布式机器人框架。

## 🚀 快速开始（5分钟）

### 1. 了解项目
```bash
# 查看项目介绍
cat README.md

# 查看快速开始指南
cat QUICK_START.md
```

### 2. 运行示例
```bash
# 编译项目
mvn clean compile

# 运行示例
mvn exec:java -Dexec.mainClass="net.aibote.examples.AndroidBotExample"
```

### 3. 查看核心代码
```
sdk-core/src/main/java/net/aibote/sdk/
  ├── factory/BotFactory.java          # 机器人工厂
  ├── handler/ResponseCacheHandler.java # 响应缓存
  └── handler/RequestTraceHandler.java  # 请求追踪
```

## 📚 学习路径

| 时间 | 推荐阅读 |
|------|---------|
| 5分钟 | README.md |
| 30分钟 | QUICK_START.md |
| 1小时 | ARCHITECTURE.md |
| 2小时 | BEST_PRACTICES.md |

## 💡 常用代码

### 创建机器人
```java
var bot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WIN)
    .withScriptName("MyScript")
    .build();
```

### 使用缓存
```java
ResponseCacheHandler cache = ResponseCacheHandler.getInstance();
cache.cache("key", data, 5 * 60 * 1000);  // 5分钟TTL
```

### 请求追踪
```java
String correlationId = RequestTraceHandler.getInstance()
    .getOrCreateCorrelationId();
```

## 🔗 相关文档

- **[README.md](README.md)** - 项目介绍
- **[QUICK_START.md](QUICK_START.md)** - 详细快速开始
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - 系统架构
- **[BEST_PRACTICES.md](BEST_PRACTICES.md)** - 最佳实践
- **[INDEX.md](INDEX.md)** - 文档索引

## ❓ 常见问题

**Q: 如何配置参数?**  
A: 使用环境变量覆盖：`export AIBOTE_RESPONSE_TIMEOUT=3000`

**Q: 如何使用缓存?**  
A: 参考 BEST_PRACTICES.md 中的缓存部分

**Q: 如何调试请求?**  
A: 使用 correlation ID，参考 BEST_PRACTICES.md

---

**准备好了？** 打开 [QUICK_START.md](QUICK_START.md) 继续学习！

