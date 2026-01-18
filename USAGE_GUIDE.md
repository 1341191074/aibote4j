# AIBoat4J 使用指南

AIBoat4J 是一个功能强大的RPA（机器人流程自动化）框架，支持Windows、Web和Android平台的自动化操作。本文档详细介绍如何使用AIBoat4J框架的各种功能。

## 目录
1. [概述](#概述)
2. [架构设计](#架构设计)
3. [设计模式应用](#设计模式应用)
4. [快速开始](#快速开始)
5. [详细功能介绍](#详细功能介绍)
6. [最佳实践](#最佳实践)

## 概述

AIBoat4J 是一个基于Java开发的RPA框架，具有以下特点：

- **跨平台支持**：支持Windows、Web和Android平台
- **高性能**：使用连接池和异步处理优化性能
- **易于扩展**：采用设计模式，便于功能扩展
- **通信协议灵活**：支持多种通信协议

## 架构设计

AIBoat4J 采用了清晰的分层架构：

```
+---------------------+
|      应用层         |
|  (用户实现的Bot)    |
+---------------------+
|      业务逻辑层      |
|  (WinBot, WebBot,   |
|   AndroidBot)       |
+---------------------+
|      抽象层         |
|  (AbstractPlatformBot|
|   和AiBot)          |
+---------------------+
|      协议层         |
|  (Communication     |
|   Protocol)         |
+---------------------+
|      连接池层       |
|  (ClientConnection  |
|   Pool)             |
+---------------------+
```

## 设计模式应用

### 1. 模板方法模式
- `AiBot` 作为抽象基类定义了算法骨架
- `AbstractPlatformBot` 扩展了平台特定的抽象方法
- 具体Bot类只需实现 `webMain()` 方法

### 2. 策略模式
- `CommunicationProtocol` 接口定义了通信策略
- `SimpleProtocolImpl` 和 `EnhancedProtocolImpl` 提供不同实现
- 可根据需要切换通信策略

### 3. 工厂模式
- `BotFactory` 用于创建不同类型的Bot实例
- `ProtocolFactory` 用于创建不同类型的协议实例

### 4. 单例模式
- `ClientConnectionPool` 使用单例模式确保全局唯一连接池
- `ChannelMap` 确保通道映射全局唯一

### 5. 连接池模式
- `ClientConnectionPool` 管理Bot连接的生命周期
- 提供高效的并发处理能力

## 快速开始

### 1. 创建自定义Bot

```java
import net.aibote.sdk.WinBot;

public class MyWinBot extends WinBot {
    @Override
    public void webMain() {
        // 实现你的自动化逻辑
        System.out.println("执行Windows自动化任务");
        
        // 例如：查找窗口
        String hwnd = findWindow("Notepad", "");
        if (hwnd != null) {
            System.out.println("找到记事本窗口: " + hwnd);
        }
    }
}
```

### 2. 使用Bot工厂

```java
import net.aibote.sdk.factory.BotFactory;

// 创建Windows Bot
MyWinBot winBot = BotFactory.createWinBot(MyWinBot.class);

// 创建Web Bot
MyWebBot webBot = BotFactory.createWebBot(MyWebBot.class);

// 创建Android Bot
MyAndroidBot androidBot = BotFactory.createAndroidBot(MyAndroidBot.class);
```

### 3. 启动服务器

```java
import net.aibote.server.WinBoteServer;

public class ServerLauncher {
    public static void main(String[] args) {
        WinBoteServer server = new WinBoteServer();
        server.startServer(MyWinBot.class, 19029);
    }
}
```

### 4. 使用连接池

```java
import net.aibote.sdk.pool.ClientConnectionPool;

ClientConnectionPool pool = ClientConnectionPool.getInstance();

// 提交Bot任务
pool.submitBotTask(myBotInstance);

// 获取池信息
System.out.println(pool.getPoolInfo());
```

## 详细功能介绍

### 通信协议管理

AIBoat4J 支持两种通信协议：

1. **简单协议 (`SimpleProtocolImpl`)**：
   - 适用于基础通信需求
   - 实现简单，资源占用少

2. **增强协议 (`EnhancedProtocolImpl`)**：
   - 提供更好的性能和错误处理
   - 使用更大的缓冲区，支持异步操作

### 平台特定功能

#### Windows Bot
- 窗口管理：查找、定位、控制窗口
- 鼠标键盘操作：点击、移动、输入
- 图像识别：找图、找色、OCR识别
- UI元素操作：查找、点击、输入

#### Web Bot
- 页面导航：前进、后退、刷新
- 元素操作：点击、输入、获取文本
- 截图功能：页面截图、元素截图
- Cookie管理：获取、设置、删除Cookie

#### Android Bot
- 触摸操作：点击、滑动、手势
- 图像识别：找图、找色、OCR识别
- 应用管理：启动、停止应用
- 文件操作：推送、拉取文件

### 性能优化特性

#### 连接池管理
- 自动管理Bot连接的生命周期
- 支持并发处理多个任务
- 提供详细的池状态监控

#### 异步处理
- 支持异步发送和接收数据
- 提高整体处理效率
- 避免阻塞主线程

## 最佳实践

### 1. Bot实现最佳实践

```java
public class ProductionWinBot extends WinBot {
    @Override
    public void webMain() {
        try {
            // 初始化
            platformInitialize();
            
            // 主要业务逻辑
            performAutomationTasks();
            
        } catch (Exception e) {
            log.error("Bot执行出错", e);
        } finally {
            // 清理资源
            platformCleanup();
        }
    }
    
    private void performAutomationTasks() {
        // 实现具体的自动化任务
    }
}
```

### 2. 连接池使用最佳实践

```java
// 在应用关闭时优雅地关闭连接池
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    ClientConnectionPool.getInstance().shutdown();
}));
```

### 3. 错误处理最佳实践

```java
@Override
public void webMain() {
    try {
        // 业务逻辑
        String result = someOperation();
        if (result == null) {
            log.warn("操作返回空结果，尝试重试");
            // 重试逻辑
        }
    } catch (Exception e) {
        log.error("操作失败", e);
        // 错误恢复逻辑
    }
}
```

### 4. 资源管理最佳实践

- 在Bot任务完成后及时释放资源
- 使用try-with-resources管理资源
- 合理设置超时时间避免长时间阻塞

## 总结

AIBoat4J 是一个功能强大且易于使用的RPA框架，通过合理的设计模式应用和性能优化，能够满足各种自动化需求。开发者可以根据具体需求选择合适的Bot类型和通信协议，利用连接池实现高效的并发处理。