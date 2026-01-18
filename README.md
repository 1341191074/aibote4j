# AIBoat4J - 企业级RPA自动化框架

AIBoat4J 是一个功能强大的机器人流程自动化（RPA）框架，支持Windows、Web和Android平台的自动化操作。该项目基于Java开发，使用Netty实现通信协议，提供稳定高效的自动化解决方案。

## 特性

- ✅ **跨平台支持**：支持Windows、Web、Android三大平台
- ✅ **设计模式**：应用多种设计模式，代码结构清晰
- ✅ **高性能**：使用连接池和异步处理优化性能
- ✅ **易扩展**：模块化设计，便于功能扩展
- ✅ **通信灵活**：支持多种通信协议策略

## 架构改进

### 设计模式应用

1. **模板方法模式**：`AiBot` 和 `AbstractPlatformBot` 定义算法骨架
2. **策略模式**：`CommunicationProtocol` 支持多种通信策略
3. **工厂模式**：`BotFactory` 和 `ProtocolFactory` 简化对象创建
4. **单例模式**：`ClientConnectionPool` 确保全局唯一实例
5. **连接池模式**：高效管理并发连接

### 性能优化

- 连接池管理，支持高并发处理
- 增强版通信协议，提升数据传输效率
- 异步处理机制，避免阻塞主线程
- 优化的缓冲区大小，提高I/O性能

## 快速开始

### 1. 环境要求

- Java 21+
- Maven 3.6+

### 2. 创建自定义Bot

```java
import net.aibote.sdk.WinBot;

public class MyWinBot extends WinBot {
    @Override
    public void webMain() {
        System.out.println("执行自动化任务...");
        // 实现具体业务逻辑
    }
}
```

### 3. 使用Bot工厂

```java
import net.aibote.sdk.factory.BotFactory;

// 创建Bot实例
MyWinBot bot = BotFactory.createWinBot(MyWinBot.class);
```

### 4. 启动服务器

```java
import net.aibote.server.WinBoteServer;

public class Launcher {
    public static void main(String[] args) {
        WinBoteServer server = new WinBoteServer();
        server.startServer(MyWinBot.class, 19029);
    }
}
```

## 核心组件

### 通信协议层
- `SimpleProtocolImpl`：基础通信协议
- `EnhancedProtocolImpl`：高性能通信协议
- `ProtocolFactory`：协议创建工厂

### 连接管理层
- `ClientConnectionPool`：连接池管理器
- 支持并发任务处理
- 提供详细的监控信息

### 平台抽象层
- `AbstractPlatformBot`：平台抽象基类
- `WinBot`：Windows平台实现
- `WebBot`：Web平台实现
- `AndroidBot`：Android平台实现

## 使用示例

参见 `src/main/java/net/aibote/examples/` 目录下的示例代码：

- `BotUsageExample.java`：基本使用示例
- `PracticalUsageExample.java`：实际应用示例

## 文档

详细使用说明请参见：[USAGE_GUIDE.md](USAGE_GUIDE.md)

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── net/aibote/
│   │       ├── sdk/          # SDK核心组件
│   │       │   ├── factory/  # 工厂类
│   │       │   ├── pool/     # 连接池
│   │       │   ├── protocol/ # 通信协议
│   │       │   ├── dto/      # 数据传输对象
│   │       │   ├── options/  # 配置选项
│   │       │   ├── utils/    # 工具类
│   │       │   ├── AbstractPlatformBot.java  # 平台抽象基类
│   │       │   ├── AiBot.java                # 核心基类
│   │       │   ├── WinBot.java               # Windows实现
│   │       │   ├── WebBot.java               # Web实现
│   │       │   └── AndroidBot.java           # Android实现
│   │       ├── server/       # 服务器组件
│   │       └── examples/     # 示例代码
│   └── resources/
└── test/
    └── java/
```

## 贡献

欢迎提交Issue和Pull Request来帮助改进项目。

## 许可证

MIT License