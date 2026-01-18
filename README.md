# AIBoTe4J - 跨平台RPA自动化框架

AIBoTe4J是一个基于Java开发的机器人流程自动化（RPA）框架，支持Windows、Web和Android平台的自动化操作。

## 项目特点

- **跨平台支持**: 支持Windows、Web和Android三大平台
- **高性能**: 使用Netty实现高效的网络通信
- **易扩展**: 基于设计模式的架构，易于扩展新功能
- **高并发**: 支持连接池管理，适合大规模部署

## 架构亮点

### 设计模式应用

1. **模板方法模式**: 通过`AbstractPlatformBot`抽象类定义算法骨架
2. **工厂模式**: 通过`BotFactory`类管理不同类型的机器人实例创建
3. **策略模式**: 通过`CommunicationStrategy`接口实现可替换的通信协议
4. **连接池模式**: 通过`ConnectionPoolManager`优化并发性能

### 重构改进

1. **统一抽象基类**: 创建了`AbstractPlatformBot`作为所有机器人类型的基类
2. **性能优化**: 使用ReentrantLock替代synchronized，减少锁竞争
3. **连接池管理**: 实现了连接池管理器，提高并发处理能力
4. **工厂模式**: 通过工厂类统一管理机器人实例的创建

## 项目结构

```
aibote4j/
├── sdk-common/           # 通用工具模块
├── sdk-core/            # 核心机器人模块
│   ├── factory/         # 工厂模式相关
│   ├── strategy/        # 策略模式相关
│   ├── pool/            # 连接池相关
│   ├── examples/        # 使用示例
│   └── ...
├── sdk-server/          # Netty服务器模块
└── pom.xml              # 项目配置文件
```

## 快速开始

### 环境要求

- Java 21 或更高版本
- Maven 3.6+

### 编译项目

```bash
mvn clean compile
```

### 使用示例

```java
import net.aibote.sdk.factory.BotFactory;
import net.aibote.sdk.WinBot;

// 创建Windows机器人实例
WinBot winBot = BotFactory.createWinBot(ctx);

// 使用机器人功能
String hwnd = winBot.findWindow("Notepad", "无标题 - 记事本");
```

更多使用示例请参考 [USAGE.md](USAGE.md)。

## 贡献

欢迎提交Issue和Pull Request来帮助改进项目。

## 许可证

[MIT License](LICENSE)
