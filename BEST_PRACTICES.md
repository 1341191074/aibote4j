# aibote4j 最佳实践指南

## 目录
1. [项目配置](#项目配置)
2. [机器人创建](#机器人创建)
3. [脚本编写](#脚本编写)
4. [错误处理](#错误处理)
5. [性能优化](#性能优化)
6. [安全考虑](#安全考虑)
7. [调试和监控](#调试和监控)

## 项目配置

### 1. 环境变量配置

在生产环境中，使用环境变量管理敏感配置：

```bash
# Linux/Mac
export AIBOTE_CONFIG=/etc/aibote/config.yml
export AIBOTE_RESPONSE_TIMEOUT=3000
export AIBOTE_MAX_CONCURRENCY=200

# Windows PowerShell
$env:AIBOTE_CONFIG = "C:\aibote\config.yml"
$env:AIBOTE_RESPONSE_TIMEOUT = "3000"
$env:AIBOTE_MAX_CONCURRENCY = "200"
```

### 2. 配置文件管理

```yaml
# bot-config.yml
communication:
  responseTimeout: 2000          # 根据网络环境调整
  delayResponseTimeout: 6000
  retryTimes: 3
  retryInterval: 500
  connectionPoolSize: 10

performance:
  maxConcurrency: 100            # 根据硬件调整
  threadPoolSize: 10

logging:
  level: INFO                    # 开发环境使用DEBUG

security:
  enableValidation: true
```

### 3. 加载配置

```java
// 推荐方式：使用ConfigManager单例
ConfigManager configManager = ConfigManager.getInstance();
BotConfig config = configManager.getConfig();

// 更新配置
configManager.reloadConfig(); // 线程安全的重新加载
```

## 机器人创建

### 1. 推荐的创建方式

使用Builder模式创建机器人，提供更好的可读性和灵活性：

```java
// ✅ 推荐
var bot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WIN)
    .withChannelContext(ctx)
    .withScriptName("MyScript")
    .build();

// ❌ 不推荐
Object bot = BotFactory.createBot(BotFactory.BotType.WIN, ctx);
```

### 2. 不同平台机器人的创建

```java
// Windows机器人
var winBot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WIN)
    .withScriptName("Windows-Automation")
    .build();

// Web机器人
var webBot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WEB)
    .withScriptName("Web-Automation")
    .build();

// Android机器人
var androidBot = BotFactory.builder()
    .withBotType(BotFactory.BotType.ANDROID)
    .withScriptName("Android-Automation")
    .build();
```

### 3. 机器人生命周期管理

```java
public class MyBotExample extends BaseExample {
    
    public MyBotExample() {
        super("MyBotExample");
    }
    
    @Override
    public void run() {
        try {
            // 创建机器人
            bot = BotFactory.builder()
                .withBotType(BotFactory.BotType.WIN)
                .withScriptName("MyScript")
                .build();
            
            // 执行操作
            // bot.doSomething();
            
        } catch (Exception e) {
            log.error("Error executing bot", e);
            throw e;
        }
    }
    
    @Override
    public void cleanup() {
        // 清理资源
        if (bot != null) {
            // bot.cleanup();
        }
        super.cleanup();
    }
    
    public static void main(String[] args) {
        MyBotExample example = new MyBotExample();
        example.runSafely(); // 使用BaseExample提供的安全运行方法
    }
}
```

## 脚本编写

### 1. 标准脚本结构

```java
public class MyBotScript extends AndroidBot {
    
    @Override
    public String getScriptName() {
        return "MyScript";
    }
    
    @Override
    public void doScript() {
        String correlationId = RequestTraceHandler.getInstance()
            .getOrCreateCorrelationId();
        
        try {
            log.info("[{}] Starting script", correlationId);
            
            // 记录请求
            RequestTraceHandler.getInstance().recordRequest(
                correlationId,
                "doScript",
                "AndroidBot"
            );
            
            // 执行脚本逻辑
            this.performOperations();
            
            log.info("[{}] Script completed", correlationId);
            
        } catch (Exception e) {
            log.error("[{}] Script failed", correlationId, e);
            throw e;
        } finally {
            // 标记请求完成
            RequestTraceHandler.getInstance()
                .markRequestComplete(correlationId);
        }
    }
    
    private void performOperations() {
        // 实现具体的业务逻辑
        this.sleep(1000);
        String data = this.getAndroidId();
        log.info("Device ID: {}", data);
    }
}
```

### 2. 使用缓存优化性能

```java
public void doScript() {
    ResponseCacheHandler cache = ResponseCacheHandler.getInstance();
    String cacheKey = "device_info_" + this.getScriptName();
    
    byte[] cachedData = cache.get(cacheKey);
    if (cachedData != null) {
        log.info("Using cached data");
        // 使用缓存数据
    } else {
        log.info("Fetching fresh data");
        byte[] freshData = this.getDeviceInfo();
        
        // 缓存结果（5分钟TTL）
        cache.cache(cacheKey, freshData, 5 * 60 * 1000);
    }
}
```

### 3. 多脚本并发执行

```java
public void runMultipleScripts() {
    var scripts = new ArrayList<AbstractPlatformBot>();
    
    // 创建多个机器人脚本
    for (int i = 0; i < 5; i++) {
        var bot = BotFactory.builder()
            .withBotType(BotFactory.BotType.ANDROID)
            .withScriptName("Script-" + i)
            .build();
        scripts.add(bot);
    }
    
    // 使用虚拟线程并发执行
    scripts.forEach(bot -> Thread.ofVirtual().start(bot::doScript));
}
```

## 错误处理

### 1. 异常捕获和处理

```java
@Override
public void doScript() {
    String correlationId = RequestTraceHandler.getInstance()
        .getOrCreateCorrelationId();
    
    try {
        // 脚本逻辑
        executeCommands();
        
    } catch (CommandException e) {
        log.error("[{}] Command execution failed: {}", 
            correlationId, e.getErrorMessage());
        // 处理命令执行错误
        handleCommandError(e);
        
    } catch (ConnectionException e) {
        log.error("[{}] Connection failed: {}", 
            correlationId, e.getErrorMessage());
        // 处理连接错误
        handleConnectionError(e);
        
    } catch (TimeoutException e) {
        log.error("[{}] Operation timeout: {}", 
            correlationId, e.getErrorMessage());
        // 处理超时错误
        handleTimeoutError(e);
        
    } catch (Exception e) {
        log.error("[{}] Unexpected error", correlationId, e);
        throw new AiboteException(1000, "脚本执行失败", e);
    }
}
```

### 2. 重试机制

```java
public void executeWithRetry(int maxRetries, long retryIntervalMs) {
    int attempt = 0;
    Exception lastException = null;
    
    while (attempt < maxRetries) {
        try {
            this.executeCommand();
            return; // 成功执行
        } catch (Exception e) {
            lastException = e;
            attempt++;
            
            if (attempt < maxRetries) {
                log.warn("Attempt {} failed, retrying in {}ms", 
                    attempt, retryIntervalMs);
                try {
                    Thread.sleep(retryIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new CommandException("Retry interrupted", ie);
                }
            }
        }
    }
    
    throw new CommandException("Failed after " + maxRetries + " attempts", lastException);
}
```

## 性能优化

### 1. 使用响应缓存

```java
private byte[] getDeviceInfo() {
    ResponseCacheHandler cache = ResponseCacheHandler.getInstance();
    String key = "device_" + this.getScriptName();
    
    byte[] cached = cache.get(key);
    if (cached != null) {
        return cached;
    }
    
    byte[] info = this.fetchDeviceInfo();
    cache.cache(key, info, 10 * 60 * 1000); // 10分钟TTL
    return info;
}
```

### 2. 批量操作优化

```java
public void processBatch(List<String> items) {
    // ✅ 推荐：批量处理减少网络往返
    var batchSize = 100;
    for (int i = 0; i < items.size(); i += batchSize) {
        List<String> batch = items.subList(i, 
            Math.min(i + batchSize, items.size()));
        this.processBatch(batch);
    }
}
```

### 3. 连接池利用

```java
public void useConnectionPool() {
    ConnectionPoolManager poolManager = ConnectionPoolManager.getInstance();
    
    // 连接池会自动复用连接，无需手动管理
    int availableConnections = poolManager.getConnectionCount();
    log.info("Available connections: {}", availableConnections);
}
```

## 安全考虑

### 1. 参数验证

```java
import net.aibote.security.ParameterValidator;

public void doScript() {
    String userInput = getUserInput();
    
    // ✅ 验证输入
    try {
        ParameterValidator.validateNotEmpty(userInput, "userInput");
        ParameterValidator.validateNoSQLInjection(userInput, "userInput");
        ParameterValidator.validateNoXSS(userInput, "userInput");
        ParameterValidator.validateLength(userInput, 1, 100, "userInput");
    } catch (IllegalArgumentException e) {
        log.error("Validation failed: {}", e.getMessage());
        return;
    }
    
    // 继续处理验证通过的输入
}
```

### 2. 敏感信息处理

```java
public void handleSensitiveData() {
    // ❌ 不要硬编码敏感信息
    // String password = "myPassword123";
    
    // ✅ 使用环境变量
    String password = System.getenv("BOT_PASSWORD");
    
    // ✅ 使用配置文件（不要提交到版本控制）
    String apiKey = ConfigManager.getInstance()
        .getConfig()
        .getSecurity()
        .getApiKey();
}
```

### 3. 日志中的敏感信息

```java
public void processPassword(String password) {
    // ❌ 不要在日志中输出敏感信息
    // log.info("Processing password: {}", password);
    
    // ✅ 掩蔽敏感信息
    String masked = password.substring(0, 2) + "****";
    log.info("Processing password: {}", masked);
}
```

## 调试和监控

### 1. 使用Correlation ID进行追踪

```java
String correlationId = RequestTraceHandler.getInstance()
    .getOrCreateCorrelationId();

log.info("[{}] Operation started", correlationId);
log.info("[{}] Fetching data", correlationId);
log.info("[{}] Processing data", correlationId);
log.info("[{}] Operation completed", correlationId);

// 查看请求元数据
RequestTraceHandler.RequestMetadata metadata = RequestTraceHandler
    .getInstance()
    .getRequestMetadata(correlationId);

if (metadata != null) {
    log.info("Request duration: {}ms", metadata.getDurationMillis());
}
```

### 2. 性能监控

```java
public void monitorPerformance() {
    RequestTraceHandler trace = RequestTraceHandler.getInstance();
    ResponseCacheHandler cache = ResponseCacheHandler.getInstance();
    
    // 监控指标
    long totalRequests = trace.getTotalRequestCount();
    int cacheSize = cache.getCacheSize();
    
    log.info("Total requests: {}", totalRequests);
    log.info("Cache size: {}", cacheSize);
    
    // 定期清理旧请求元数据
    // trace.clearAllMetadata();
}
```

### 3. 日志级别设置

```java
// application.yml or logback.xml
logging:
  level:
    root: INFO
    net.aibote: DEBUG  # SDK使用DEBUG级别
    com.example: INFO
```

### 4. 调试模式

```java
public void debugScript() {
    if (log.isDebugEnabled()) {
        // 仅在DEBUG模式下执行
        var metadata = RequestTraceHandler.getInstance()
            .getAllRequestMetadata();
        log.debug("Request metadata: {}", metadata);
    }
}
```

## 常见问题解决

### Q1: 为什么响应超时？

**解决步骤**：
1. 检查网络连接
2. 增加超时时间：`AIBOTE_RESPONSE_TIMEOUT=5000`
3. 查看correlation ID相关的日志
4. 使用RequestTraceHandler检查执行时间

### Q2: 如何处理并发请求？

**解决方案**：
1. 使用虚拟线程：`Thread.ofVirtual().start(...)`
2. 调整`maxConcurrency`配置
3. 使用连接池管理连接
4. 监控correlation ID防止冲突

### Q3: 缓存何时应该清空？

**清空时机**：
- 配置更新后
- 长时间运行后（防止内存泄漏）
- 缓存数据过时时
- 应用重启时

```java
// 手动清空缓存
ResponseCacheHandler.getInstance().clear();
RequestTraceHandler.getInstance().clearAllMetadata();
```

## 检查清单

部署前检查：

- [ ] 配置文件已正确设置
- [ ] 环境变量已配置（如需要）
- [ ] 日志级别设置为INFO（生产环境）
- [ ] 参数验证已启用
- [ ] 错误处理完整
- [ ] 敏感信息未硬编码
- [ ] 缓存TTL设置合理
- [ ] 连接池大小根据负载调整
- [ ] 监控和告警已配置
- [ ] 文档已更新

---

**最后更新**: 2026-01-19

