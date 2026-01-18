# AIBoTe4J 框架使用说明文档

## 项目概述

AIBoTe4J 是一个基于Java开发的跨平台RPA（机器人流程自动化）框架，支持Windows、Web和Android平台的自动化操作。

## 架构设计

### 设计模式应用

本框架应用了多种设计模式以提高代码的可维护性和扩展性：

1. **模板方法模式**: 通过 [AbstractPlatformBot](sdk-core/src/main/java/net/aibote/sdk/AbstractPlatformBot.java) 抽象类定义算法骨架
2. **工厂模式**: 通过 [BotFactory](sdk-core/src/main/java/net/aibote/sdk/factory/BotFactory.java) 类管理不同类型的机器人实例创建
3. **策略模式**: 通过 [CommunicationStrategy](sdk-core/src/main/java/net/aibote/sdk/strategy/CommunicationStrategy.java) 接口实现可替换的通信协议
4. **连接池模式**: 通过 [ConnectionPoolManager](sdk-core/src/main/java/net/aibote/sdk/pool/ConnectionPoolManager.java) 优化并发性能

### 核心组件

- **AbstractPlatformBot**: 所有平台机器人的抽象基类，统一管理通信逻辑
- **WinBot/WebBot/AndroidBot**: 各平台具体的机器人实现
- **BotFactory**: 机器人工厂类，负责创建不同类型的机器人实例
- **ConnectionPoolManager**: 连接池管理器，管理多个客户端连接

## 快速开始

### 1. 环境要求

- Java 21 或更高版本
- Maven 3.6+

### 2. 项目结构

```
aibote4j/
├── sdk-common/           # 通用工具模块
├── sdk-core/            # 核心机器人模块
│   ├── src/main/java/net/aibote/sdk/
│   │   ├── factory/     # 工厂模式相关
│   │   ├── strategy/    # 策略模式相关  
│   │   ├── pool/        # 连接池相关
│   │   └── examples/    # 使用示例
│   └── ...
├── sdk-server/          # Netty服务器模块
└── pom.xml              # 项目配置文件
```

### 3. 创建机器人实例

#### 使用工厂模式创建机器人

```java
import net.aibote.sdk.factory.BotFactory;
import net.aibote.sdk.WinBot;
import io.netty.channel.ChannelHandlerContext;

// 获取通道上下文（来自Netty连接）
ChannelHandlerContext ctx = ...;

// 创建Windows机器人
WinBot winBot = BotFactory.createWinBot(ctx);

// 使用机器人
String hwnd = winBot.findWindow("Notepad", "无标题 - 记事本");
System.out.println("找到窗口: " + hwnd);
```

### 4. 各平台使用示例

#### Windows机器人示例

```java
import net.aibote.sdk.WinBot;
import net.aibote.sdk.factory.BotFactory;

public class WinBotDemo {
    public static void main(String[] args) {
        // 从Netty连接获取ctx
        WinBot winBot = BotFactory.createWinBot(ctx);
        
        // 查找窗口
        String hwnd = winBot.findWindow("Notepad", null);
        
        // 点击坐标
        winBot.clickMouse(hwnd, 100, 100, 1, Mode.FRONT, null);
        
        // 截图
        winBot.saveScreenshot(hwnd, "screenshot.png", new Region(), 0, 127, 255);
    }
}
```

#### Web机器人示例

```java
import net.aibote.sdk.WebBot;
import net.aibote.sdk.factory.BotFactory;

public class WebBotDemo {
    public static void main(String[] args) {
        WebBot webBot = BotFactory.createWebBot(ctx);
        
        // 导航到网页
        webBot.navigate("https://www.example.com");
        
        // 点击元素
        webBot.clickElement("//button[@id='submit']");
        
        // 输入文本
        webBot.setElementValue("//input[@id='username']", "myuser");
    }
}
```

#### Android机器人示例

```java
import net.aibote.sdk.AndroidBot;
import net.aibote.sdk.factory.BotFactory;

public class AndroidBotDemo {
    public static void main(String[] args) {
        AndroidBot androidBot = BotFactory.createAndroidBot(ctx);
        
        // 点击坐标
        androidBot.click(500, 1000);
        
        // 滑动
        androidBot.swipe(500, 1500, 500, 500, 1000);
        
        // 截图
        androidBot.saveScreenshot("screenshot.png", new Region(), 0, 127, 255);
    }
}
```

## 高级特性

### 连接池管理

使用连接池管理多个客户端连接：

```java
import net.aibote.sdk.pool.ConnectionPoolManager;

// 获取连接池管理器
ConnectionPoolManager poolManager = ConnectionPoolManager.getInstance();

// 添加连接
poolManager.addConnection("channel-id-1", ctx);

// 获取连接
ChannelHandlerContext retrievedCtx = poolManager.getConnection("channel-id-1");

// 移除连接
poolManager.removeConnection("channel-id-1");

// 获取连接总数
int count = poolManager.getConnectionCount();
```

### 自定义通信策略

可以通过实现 [CommunicationStrategy](sdk-core/src/main/java/net/aibote/sdk/strategy/CommunicationStrategy.java) 接口自定义通信协议：

```java
import net.aibote.sdk.strategy.CommunicationStrategy;

public class CustomStrategy implements CommunicationStrategy {
    @Override
    public void sendCommand(ChannelHandlerContext ctx, String command, String... params) {
        // 自定义命令发送逻辑
    }
    
    @Override
    public void sendBytes(ChannelHandlerContext ctx, byte[] data) {
        // 自定义字节发送逻辑
    }
    
    @Override
    public Object handleResponse(byte[] response) {
        // 自定义响应处理逻辑
        return response;
    }
}
```

## 性能优化建议

1. **连接复用**: 使用 [ConnectionPoolManager](sdk-core/src/main/java/net/aibote/sdk/pool/ConnectionPoolManager.java) 管理连接，避免频繁建立和关闭连接
2. **批量操作**: 尽可能合并多个小操作为批量操作，减少网络往返次数
3. **缓存机制**: 对于频繁访问的数据，考虑添加缓存层
4. **异步处理**: 对于耗时操作，使用异步处理避免阻塞主线程

## 最佳实践

1. **异常处理**: 始终处理可能出现的网络异常和机器人操作异常
2. **资源清理**: 在适当的时候清理连接和其他资源
3. **日志记录**: 合理使用日志记录关键操作和错误信息
4. **配置管理**: 将配置参数外部化，便于管理和调整

## 故障排除

常见问题和解决方案：

- **连接失败**: 检查网络连接和服务器状态
- **命令超时**: 调整超时参数或检查机器人端是否繁忙
- **内存泄漏**: 确保及时释放不再使用的连接和资源

## 扩展开发

如需扩展新平台支持：

1. 继承 [AbstractPlatformBot](sdk-core/src/main/java/net/aibote/sdk/AbstractPlatformBot.java) 抽象类
2. 在 [BotFactory](sdk-core/src/main/java/net/aibote/sdk/factory/BotFactory.java) 中添加新类型的创建方法
3. 实现平台特定的操作方法
4. 更新相应的策略实现（如果需要）

## 版本历史

- 1.0: 初始版本，支持Windows、Web和Android平台的基础自动化功能
- 1.1: 重构架构，引入设计模式，提升性能和可维护性