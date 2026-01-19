# aibote4j 架构文档

## 项目概述

aibote4j 是一个基于 Java 和 Netty 的分布式机器人框架，支持多平台（Windows、Web、Android）的自动化操作。

## 项目结构

```
aibote4j/
├── sdk-common/          # 公共模块 - 包含工具类、配置、安全验证等
│   ├── security/        # 安全相关
│   ├── utils/           # 工具类
│   └── config/          # 配置管理
├── sdk-core/            # 核心模块 - 机器人基类、工厂、异常处理等
│   ├── sdk/             # 核心SDK
│   ├── handler/         # 处理器（新增缓存和追踪）
│   ├── exception/       # 异常定义
│   └── factory/         # 工厂类
└── sdk-server/          # 服务端模块 - 服务器、处理器、示例
    ├── server/          # 服务器实现
    ├── handler/         # 请求处理器
    └── examples/        # 示例代码（已改进）
```

## 核心模块详解

### 1. sdk-common 模块

#### 配置管理 (ConfigManager)
- **单例模式**：确保全局唯一的配置实例
- **多源加载**：支持环境变量、classpath、文件系统三层加载机制
- **动态覆盖**：通过环境变量覆盖配置值
- **线程安全**：使用专门的LOCK对象进行同步

```java
// 使用示例
ConfigManager configManager = ConfigManager.getInstance();
long timeout = configManager.getCommunicationConfig().getResponseTimeout();
```

#### YAML工具 (YamlUtils)
- **安全加载**：限制YAML别名数量防止DoS攻击
- **UTF-8支持**：正确处理中文和其他字符集
- **灵活接口**：支持流和文件两种加载方式

```java
// 使用示例
BotConfig config = YamlUtils.loadAs(inputStream, BotConfig.class);
```

#### 参数验证 (ParameterValidator)
- **SQL注入防护**：检测SQL关键字
- **XSS防护**：检测脚本标签和事件
- **格式验证**：Email、IP等格式检查

### 2. sdk-core 模块

#### 机器人工厂 (BotFactory)
- **工厂模式**：创建不同类型机器人
- **Builder模式**：灵活配置机器人参数
- **类型安全**：返回AbstractPlatformBot而不是Object

```java
// 使用示例
var bot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WIN)
    .withChannelContext(ctx)
    .withScriptName("MyScript")
    .build();
```

#### 平台机器人 (AbstractPlatformBot)
- **抽象基类**：统一所有平台机器人的基础功能
- **模板方法**：定义脚本执行流程
- **配置驱动**：从ConfigManager读取超时等参数

**三个具体实现**：
- `WinBot` - Windows自动化
- `WebBot` - Web自动化
- `AndroidBot` - Android自动化

#### 异常体系 (Exception Hierarchy)
- `AiboteException` - 基础异常类，包含错误码、信息和时间戳
- `CommandException` - 命令执行异常
- `ConnectionException` - 连接异常
- `TimeoutException` - 超时异常

#### 响应缓存处理器 (ResponseCacheHandler)
- **TTL机制**：自动清理过期缓存
- **并发安全**：ConcurrentHashMap存储
- **后台清理**：守护线程定期清理
- **内存保护**：防止缓存泄漏

```java
// 使用示例
ResponseCacheHandler cache = ResponseCacheHandler.getInstance();
cache.cache("key1", responseBytes, 5*60*1000); // 5分钟TTL
byte[] result = cache.get("key1");
```

#### 请求追踪处理器 (RequestTraceHandler)
- **correlation ID**：为每个请求分配唯一ID
- **分布式追踪**：支持跨线程请求关联
- **性能指标**：记录请求执行时间
- **ThreadLocal存储**：线程安全的ID管理

```java
// 使用示例
RequestTraceHandler trace = RequestTraceHandler.getInstance();
String correlationId = trace.getOrCreateCorrelationId();
trace.recordRequest(correlationId, "command", "WinBot");
trace.markRequestComplete(correlationId);
```

### 3. sdk-server 模块

#### 服务器 (BotServer)
- **Netty集成**：基于Netty构建网络服务
- **编解码**：自定义编解码器处理协议
- **通道管理**：管理客户端连接

#### 客户端管理 (ClientManager)
- **单例模式**：全局唯一的客户端管理
- **ConcurrentHashMap**：线程安全的客户端存储
- **消息队列**：LinkedBlockingQueue存储可用客户端
- **生命周期管理**：新增、删除、查询客户端

```java
// 使用示例
ClientManager manager = ClientManager.getInstance();
manager.add("client1", channel);
String nextClient = manager.poll();
manager.remove("client1");
```

#### 示例代码
- `BaseExample` - 所有示例的基类（新增）
- `AndroidBotExample` - Android机器人示例
- `WinBotExample` - Windows机器人示例
- `WebBotExample` - Web机器人示例
- `ComprehensiveExample` - 综合示例
- `ScriptManager` - 脚本执行管理
- `AndroidBotTest` - Android测试类

## 设计模式应用

### 1. 单例模式
用于：ConfigManager, ClientManager, ResponseCacheHandler, RequestTraceHandler

```java
private static volatile Instance instance;
private static final Object LOCK = new Object();

public static Instance getInstance() {
    if (instance == null) {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new Instance();
            }
        }
    }
    return instance;
}
```

### 2. 工厂模式
`BotFactory` 用于创建不同类型的机器人实例

### 3. Builder模式
`BotFactory.BotConfigBuilder` 用于灵活配置机器人

### 4. 模板方法模式
`AbstractPlatformBot` 定义脚本执行流程，子类实现具体逻辑

### 5. 观察者模式
Netty的ChannelHandler使用观察者模式处理I/O事件

## 并发和线程安全

### ConcurrentHashMap使用
- ClientManager 的客户端存储
- ResponseCacheHandler 的缓存存储
- RequestTraceHandler 的元数据存储

### ThreadLocal使用
- RequestTraceHandler 的 correlation ID 存储
- 确保多线程环境下ID不冲突

### 原子操作
- AtomicLong 用于请求计数

### 同步锁
- ConfigManager 使用显式LOCK对象
- 避免在不必要的地方使用synchronized

## 安全性考虑

### 1. YAML加载安全性
- 限制别名集合数量防止DoS
- 使用SafeConstructor防止代码执行

### 2. 参数验证
- SQL注入检测
- XSS攻击防护
- 格式验证

### 3. 环境变量安全
- 支持敏感配置通过环境变量覆盖
- 不硬编码敏感信息

## 性能优化

### 1. 缓存机制
- ResponseCacheHandler提供TTL缓存
- 减少重复计算和网络请求

### 2. 异步处理
- 使用虚拟线程处理脚本执行
- 提高并发处理能力

### 3. 连接池
- ConnectionPoolManager管理连接复用
- 减少连接创建开销

### 4. 分布式追踪
- correlation ID用于性能分析
- 记录请求执行时间

## 配置文件结构

```yaml
communication:
  responseTimeout: 2000          # 正常响应超时
  delayResponseTimeout: 6000     # 延迟响应超时
  retryTimes: 3                  # 重试次数
  retryInterval: 500             # 重试间隔
  connectionPoolSize: 10         # 连接池大小

performance:
  maxConcurrency: 100            # 最大并发数
  threadPoolSize: 10             # 线程池大小

logging:
  level: INFO                    # 日志级别

security:
  enableValidation: true         # 启用参数验证
```

## 环境变量支持

| 变量名 | 说明 | 示例 |
|------|------|------|
| AIBOTE_CONFIG | 配置文件路径 | /etc/aibote/config.yml |
| AIBOTE_RESPONSE_TIMEOUT | 响应超时时间 | 3000 |
| AIBOTE_MAX_CONCURRENCY | 最大并发数 | 200 |

## 日志记录

### 日志级别
- DEBUG: 详细的执行流程
- INFO: 关键操作（启动、关闭、重要事件）
- WARN: 潜在问题（未找到配置、超时）
- ERROR: 错误信息

### 日志格式
```
[时间] [线程] [日志级别] [类名] [消息]
```

## 错误处理

### 异常层次
```
Throwable
├── Exception
│   ├── AiboteException (机器人异常基类)
│   │   ├── CommandException (命令异常)
│   │   ├── ConnectionException (连接异常)
│   │   └── TimeoutException (超时异常)
│   └── ...
└── Error
```

### 最佳实践
1. 使用具体的异常类而不是通用Exception
2. 在日志中记录correlation ID用于追踪
3. 提供有意义的错误信息

## 扩展点

### 1. 新增机器人类型
继承 `AbstractPlatformBot` 并在 `BotFactory` 中注册

### 2. 自定义缓存策略
扩展 `ResponseCacheHandler` 实现自定义TTL或存储策略

### 3. 自定义追踪
扩展 `RequestTraceHandler` 进行请求追踪

### 4. 自定义验证规则
扩展 `ParameterValidator` 添加业务特定验证

## 相关文档

- [使用说明](USAGE.md) - 框架使用指南
- [变更日志](CHANGELOG.md) - 版本历史

## 版本信息

- **当前版本**: 1.0
- **优化日期**: 2026-01-19
- **Java版本**: 21+
- **主要依赖**: Netty 4.1.116, Lombok 1.18.36, SnakeYAML 2.5

---

**最后更新**: 2026-01-19

