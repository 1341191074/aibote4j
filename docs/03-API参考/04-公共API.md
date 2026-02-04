# 公共 API 参考

本章详细介绍框架的公共 API，包括通用工具类、工厂类等。

## 公共类 API

### AbstractPlatformBot 类

[AbstractPlatformBot](file:///F:/workspace/github/aibote4j/sdk-core/src/main/java/net/aibote/sdk/AbstractPlatformBot.java) 是所有平台机器人的抽象基类。

#### getScriptName
获取脚本名称

**方法签名：**
```java
public abstract String getScriptName()
```

**返回值：** 脚本名称

#### sleep
休眠

**方法签名：**
```java
public void sleep(int milliseconds)
```

**参数：**
- `milliseconds` - 休眠时间（毫秒）

#### getVersion
获取框架版本

**方法签名：**
```java
public static String getVersion()
```

**返回值：** 框架版本号

## 任务引擎 API

### TaskEngine 类

任务引擎是框架的核心组件，负责任务的注册、管理和执行。

#### getInstance
获取任务引擎单例实例

**方法签名：**
```java
public static TaskEngine getInstance()
```

**返回值：** TaskEngine 单例实例

**使用示例：**
```java
TaskEngine engine = TaskEngine.getInstance();
```

#### registerTask
注册任务

**方法签名：**
```java
public String registerTask(String taskName, TaskDefinition taskDefinition)
```

**参数：**
- `taskName` - 任务名称
- `taskDefinition` - 任务定义

**返回值：** 任务ID

**使用示例：**
```java
NotepadAutomationTask task = NotepadAutomationTask.builder()
    .taskName("记事本任务")
    .scriptName("Notepad-Bot")
    .build();

String taskId = TaskEngine.getInstance().registerTask("notepad-task", task);
```

#### getTask
获取已注册的任务

**方法签名：**
```java
public TaskDefinition getTask(String taskId)
```

**参数：**
- `taskId` - 任务ID

**返回值：** 任务定义

#### executeTask
执行指定任务

**方法签名：**
```java
public void executeTask(String taskId, ChannelHandlerContext ctx, BotFactory.BotType botType)
```

**参数：**
- `taskId` - 任务ID
- `ctx` - 通道上下文
- `botType` - 机器人类型

#### getAllTasks
获取所有已注册的任务

**方法签名：**
```java
public Map<String, TaskDefinition> getAllTasks()
```

**返回值：** 任务映射

#### removeTask
移除任务

**方法签名：**
```java
public boolean removeTask(String taskId)
```

**参数：**
- `taskId` - 任务ID

**返回值：** 是否移除成功

## 工厂类 API

### BotFactory 类

机器人工厂，用于在服务端内部创建机器人实例

**使用示例：**
```java
// 通常在任务执行过程中由框架自动创建
// 用户无需直接调用工厂方法
```

### TaskDefinition 接口

任务定义接口，所有自定义任务都需要实现此接口。

#### getSupportedBotTypes
获取支持的机器人类型

**方法签名：**
```java
Set<BotFactory.BotType> getSupportedBotTypes()
```

**返回值：** 支持的机器人类型集合

#### getTaskExecutor
获取任务执行器

**方法签名：**
```java
TaskExecutor getTaskExecutor()
```

**返回值：** 任务执行器

### NotepadAutomationTask 类

内置的记事本自动化任务示例。

**使用示例：**
```java
NotepadAutomationTask task = NotepadAutomationTask.builder()
    .taskName("记事本自动化")
    .scriptName("Notepad-Bot")
    .description("自动查找记事本窗口并执行操作")
    .build();
```

## 数据模型 API

### Region 类

区域定义

**字段：**
```java
public class Region {
    public int left;    // 左边界
    public int top;     // 上边界
    public int right;   // 右边界
    public int bottom;  // 下边界
}
```

**使用示例：**
```java
Region region = new Region();
region.left = 100;
region.top = 100;
region.right = 500;
region.bottom = 500;
```

### Point 类

点坐标

**字段：**
```java
public class Point {
    public int x;  // X 坐标
    public int y;  // Y 坐标
}
```

**使用示例：**
```java
Point point = new Point(500, 600);
System.out.println("X: " + point.x + ", Y: " + point.y);
```

### OCRResult 类

OCR 识别结果

**字段：**
```java
public class OCRResult {
    public Point lt;      // 左上角坐标
    public Point rt;      // 右上角坐标
    public Point ld;      // 左下角坐标
    public Point rd;      // 右下角坐标
    public String word;   // 识别的文字
    public float rate;    // 置信度
}
```

### SubColor 类

辅助颜色

**字段：**
```java
public class SubColor {
    public int offsetX;     // X 偏移
    public int offsetY;     // Y 偏移
    public String colorStr; // 颜色字符串，格式 "#RRGGBB"
}
```

## 枚举类 API

### Mode 枚举

操作模式

**枚举值：**
```java
public enum Mode {
    FOREGROUND,  // 前台模式
    BACKGROUND   // 后台模式
}
```

## 配置管理 API

### ConfigManager 类

配置管理器，用于统一管理框架配置

**获取实例：**
```java
ConfigManager configManager = ConfigManager.getInstance();
```

### CommunicationConfig 类

通信配置

**方法：**
```java
// 获取响应超时时间
long getResponseTimeout();

// 设置响应超时时间
void setResponseTimeout(long timeout);

// 获取延迟响应超时时间
long getDelayResponseTimeout();

// 设置延迟响应超时时间
void setDelayResponseTimeout(long timeout);

// 获取重试次数
int getRetryTimes();

// 设置重试次数
void setRetryTimes(int retryTimes);

// 获取重试间隔
int getRetryInterval();

// 设置重试间隔
void setRetryInterval(int interval);

// 获取连接池大小
int getConnectionPoolSize();

// 设置连接池大小
void setConnectionPoolSize(int poolSize);
```

### PerformanceConfig 类

性能配置

**方法：**
```java
// 获取最大并发数
int getMaxConcurrency();

// 设置最大并发数
void setMaxConcurrency(int maxConcurrency);

// 获取线程池大小
int getThreadPoolSize();

// 设置线程池大小
void setThreadPoolSize(int threadPoolSize);
```

## 缓存和追踪 API

### ResponseCacheHandler 类

响应缓存处理器

**获取实例：**
```java
ResponseCacheHandler cache = ResponseCacheHandler.getInstance();
```

**方法：**
```java
// 缓存数据
void cache(String key, byte[] data, long ttlMs);

// 获取缓存
byte[] get(String key);

// 获取缓存大小
int getCacheSize();

// 清空缓存
void clear();
```

### RequestTraceHandler 类

请求追踪处理器

**获取实例：**
```java
RequestTraceHandler trace = RequestTraceHandler.getInstance();
```

**方法：**
```java
// 获取或创建 Correlation ID
String getOrCreateCorrelationId();

// 记录请求
void recordRequest(String correlationId, String command, String botType);

// 标记请求完成
void markRequestComplete(String correlationId);

// 获取请求元数据
RequestTraceHandler.RequestMetadata getRequestMetadata(String correlationId);

// 获取总请求数
long getTotalRequestCount();

// 清空所有元数据
void clearAllMetadata();
```

### RequestTraceHandler.RequestMetadata 类

请求元数据

**方法：**
```java
// 获取持续时间（毫秒）
long getDurationMillis();

// 获取开始时间
long getStartTime();

// 获取结束时间
long getEndTime();
```

## 安全相关 API

### ParameterValidator 类

参数验证器

**方法：**
```java
// 验证字符串非空
static void validateNotEmpty(String value, String fieldName);

// 验证无 SQL 注入
static void validateNoSQLInjection(String value, String fieldName);

// 验证无 XSS
static void validateNoXSS(String value, String fieldName);

// 验证长度
static void validateLength(String value, int minLength, int maxLength, String fieldName);

// 验证邮箱格式
static boolean isValidEmail(String email);

// 验证 IP 地址格式
static boolean isValidIP(String ip);
```

## 工具类 API

### ImageBase64Converter 类

图片与 Base64 转换工具

**方法：**
```java
// 图片文件转 Base64
static String imageToBase64(String imagePath);

// Base64 转图片文件
static boolean base64ToImage(String base64String, String imagePath);
```

### Base64Utils 类

Base64 工具类

**方法：**
```java
// 编码
static String encode(String str);

// 解码
static String decode(String str);
```

### HttpClientUtils 类

HTTP 客户端工具

**方法：**
```java
// GET 请求
static String doGet(String url);

// POST 请求
static String doPost(String url, String body);

// POST JSON 请求
static String doPostJson(String url, String json);
```

### ClientType 枚举

客户端类型

**枚举值：**
```java
public enum ClientType {
    WIN,     // Windows 客户端
    WEB,     // Web 客户端
    ANDROID  // Android 客户端
}
```