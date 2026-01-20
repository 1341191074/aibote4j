# Aibote4J - Java 跨平台 RPA 框架

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/java-21%2B-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/maven-3.8%2B-red.svg)](https://maven.apache.org/)

## 📖 项目介绍

**Aibote4J** 是一个功能强大的 Java RPA（机器人流程自动化）框架，支持跨平台自动化操作。

### ✨ 核心特性

- 🖥️ **多平台支持**
  - Windows 桌面应用自动化
  - Web 应用自动化（基于 Chrome/Edge）
  - Android 设备自动化

- 🚀 **高性能设计**
  - 基于 Netty 的高效网络通信
  - 连接池管理，支持高并发
  - 内存缓存优化

- 🛡️ **安全可靠**
  - 参数验证和类型检查
  - 完善的异常处理机制
  - Token 管理和访问控制

- 🎨 **易用的 API**
  - 统一的机器人接口
  - 工厂模式快速创建实例
  - 详细的 API 文档和示例

- 🔌 **扩展性强**
  - 模板方法模式支持定制
  - 策略模式实现灵活通信
  - 插件化架构

### 🎯 使用场景

- 📊 **自动化数据处理** - 从多个来源收集和处理数据
- 🔄 **流程自动化** - 自动化重复的业务流程
- 🧪 **自动化测试** - UI 自动化测试框架
- 📱 **移动应用测试** - Android 设备自动化测试
- 🌐 **Web 应用测试** - 跨浏览器自动化测试

## 🚀 快速开始

### 前置要求

- **Java 21+** - 需要 Java 21 或更高版本
- **Maven 3.8+** - 用于项目构建
- **Git** - 用于克隆项目

### 1️⃣ 克隆项目

```bash
git clone https://github.com/aibote/aibote4j.git
cd aibote4j
```

### 2️⃣ 编译项目

```bash
# 清理并完整编译
mvn clean compile

# 快速编译（跳过测试）
mvn clean compile -DskipTests
```

### 3️⃣ 首个自动化脚本（5分钟）

```java
import net.aibote.sdk.WinBot;
import net.aibote.sdk.factory.BotFactory;

public class FirstAutomation {
    public static void main(String[] args) {
        // 创建 Windows 自动化机器人
        WinBot bot = BotFactory.builder()
            .withBotType(BotFactory.BotType.WIN)
            .withIP("127.0.0.1")
            .withPort(9527)
            .build();
        
        // 连接到设备
        bot.connect();
        
        // 获取屏幕尺寸
        String windowSize = bot.getWindowSize();
        System.out.println("屏幕大小: " + windowSize);
        
        // 点击屏幕中心
        String[] size = windowSize.split("\\|");
        int centerX = Integer.parseInt(size[0]) / 2;
        int centerY = Integer.parseInt(size[1]) / 2;
        bot.click(centerX, centerY);
        
        // 断开连接
        bot.disconnect();
    }
}
```

> 详细的快速开始指南请查看 [docs/01-入门指南/03-快速开始.md](docs/01-%E5%85%A5%E9%97%A8%E6%8C%87%E5%8D%97/03-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B.md)

## 📚 文档体系

完整的文档现已整理至 [docs](docs/) 目录，包含以下部分：

### 📘 入门指南 ([docs/01-入门指南](docs/01-%E5%85%A5%E9%97%A8%E6%8C%87%E5%8D%97))
- [项目介绍](docs/01-%E5%85%A5%E9%97%A8%E6%8C%87%E5%8D%97/01-%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D.md) - 项目概述和核心特性
- [环境安装](docs/01-%E5%85%A5%E9%97%A8%E6%8C%87%E5%8D%97/02-%E7%8E%AF%E5%A2%83%E5%AE%89%E8%A3%85.md) - 环境配置和项目编译
- [快速开始](docs/01-%E5%85%A5%E9%97%A8%E6%8C%87%E5%8D%97/03-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B.md) - 5分钟上手教程

### 📙 使用指南 ([docs/02-使用指南](docs/02-%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97))
- [基础概念](docs/02-%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/01-%E5%9F%BA%E7%A1%80%E6%A6%82%E5%BF%B5.md) - 核心概念和架构
- [Windows自动化](docs/02-%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/02-Windows%E8%87%AA%E5%8A%A8%E5%8C%96.md) - Windows 平台操作
- [Android自动化](docs/02-%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/03-Android%E8%87%AA%E5%8A%A8%E5%8C%96.md) - Android 平台操作
- [Web自动化](docs/02-%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/04-Web%E8%87%AA%E5%8A%A8%E5%8C%96.md) - Web 平台操作

### 📕 API参考 ([docs/03-API参考](docs/03-API%E5%8F%82%E8%80%83))
- [Windows API](docs/03-API%E5%8F%82%E8%80%83/01-Windows-API.md) - Windows 专用 API
- [Android API](docs/03-API%E5%8F%82%E8%80%83/02-Android-API.md) - Android 专用 API
- [Web API](docs/03-API%E5%8F%82%E8%80%83/03-Web-API.md) - Web 专用 API
- [公共 API](docs/03-API%E5%8F%82%E8%80%83/04-%E5%85%AC%E5%85%B1API.md) - 通用 API

### 📗 高级主题 ([docs/04-高级主题](docs/04-%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98))
- [架构设计](docs/04-%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98/01-%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1.md) - 系统架构和设计模式
- [最佳实践](docs/04-%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98/02-%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5.md) - 使用技巧和性能优化
- [HID功能参考](docs/04-%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98/03-HID%E5%8A%9F%E8%83%BD%E5%8F%82%E8%80%83.md) - HID 控制功能

### 📙 故障排除 ([docs/05-故障排除](docs/05-%E6%95%85%E9%9A%9C%E6%8E%92%E9%99%A4))
- [常见问题](docs/05-%E6%95%85%E9%9A%9C%E6%8E%92%E9%99%A4/01-%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98.md) - 问题解答
- [错误诊断](docs/05-%E6%95%85%E9%9A%9C%E6%8E%92%E9%99%A4/02-%E9%94%99%E8%AF%AF%E8%AF%8A%E6%96%AD.md) - 诊断方法

### 📘 参考资料 ([docs/06-参考资料](docs/06-%E5%8F%82%E8%80%83%E8%B5%84%E6%96%99))
- [架构设计](docs/06-%E5%8F%82%E8%80%83%E8%B5%84%E6%96%99/01-%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1.md) - 详细架构参考
- [版本更新](docs/06-%E5%8F%82%E8%80%83%E8%B5%84%E6%96%99/02-%E7%89%88%E6%9C%AC%E6%9B%B4%E6%96%B0.md) - 版本历史
- [术语表](docs/06-%E5%8F%82%E8%80%83%E8%B5%84%E6%96%99/03-%E6%9C%AF%E8%AF%AD%E8%A1%A8.md) - 术语解释

---

## 💻 支持的平台

### Windows 自动化 (WinBot)
- ✅ 窗口查找和管理
- ✅ 鼠标键盘操作
- ✅ 截图和图像识别
- ✅ OCR 文字识别
- ✅ 目标检测 (YOLO)

### Web 自动化 (WebBot)
- ✅ 浏览器控制
- ✅ 页面导航和交互
- ✅ 元素定位和操作
- ✅ JavaScript 执行
- ✅ Cookie 管理

### Android 自动化 (AndroidBot)
- ✅ 屏幕交互
- ✅ 图像识别
- ✅ OCR 识别
- ✅ UI 元素操作
- ✅ HID 硬件控制

## 🔗 API 概览

### 核心 API

```java
// 创建机器人实例
WinBot bot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WIN)
    .withIP("127.0.0.1")
    .withPort(9527)
    .build();

// 连接
bot.connect();

// 屏幕操作
bot.takeScreenshot("screenshot.png");
bot.click(500, 600);
bot.swipe(100, 200, 600, 200);

// 图像识别
Point point = bot.findImage("target.png");

// OCR 识别
List<OCRResult> results = bot.ocr(region);

// 断开连接
bot.disconnect();
```

## 🎓 学习资源

### 入门教程
1. 📖 阅读 [快速开始](docs/01-%E5%85%A5%E9%97%A8%E6%8C%87%E5%8D%97/03-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B.md)（5 分钟）
2. 📦 按照 [环境安装](docs/01-%E5%85%A5%E9%97%A8%E6%8C%87%E5%8D%97/02-%E7%8E%AF%E5%A2%83%E5%AE%89%E8%A3%85.md) 安装（10 分钟）
3. 💡 查看示例代码（20 分钟）
4. 📚 学习 [使用指南](docs/02-%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/README.md)（30 分钟）

### 进阶学习
1. 🏗️ 阅读 [架构设计](docs/04-%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98/01-%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1.md)
2. 💻 研究设计模式应用
3. 🔧 学习 [最佳实践](docs/04-%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98/02-%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5.md)
4. ⚙️ 探索 HID 功能 [HID功能参考](docs/04-%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98/03-HID%E5%8A%9F%E8%83%BD%E5%8F%82%E8%80%83.md)

## ❓ 常见问题

### Q: 如何连接到远程设备？
A: 配置 IP 和端口即可连接远程 WindowsDriver。详见 [环境安装](docs/01-%E5%85%A5%E9%97%A8%E6%8C%87%E5%8D%97/02-%E7%8E%AF%E5%A2%83%E5%AE%89%E8%A3%85.md)。

### Q: 支持哪些浏览器？
A: WebBot 支持 Chrome 和 Edge。详见 [Web自动化](docs/02-%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/04-Web%E8%87%AA%E5%8A%A8%E5%8C%96.md)。

### Q: 如何解决连接问题？
A: 查看 [故障排除](docs/05-%E6%95%85%E9%9A%9C%E6%8E%92%E9%99%A4/README.md)。

### Q: 更多问题？
A: 查看 [常见问题](docs/05-%E6%95%85%E9%9A%9C%E6%8E%92%E9%99%A4/01-%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98.md) 或提交 Issue。

## 🤝 贡献指南

欢迎贡献代码、提交 Bug、改进文档！

### 提交流程
1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 开发规范
- 遵循 [最佳实践](docs/04-%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98/02-%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5.md) 中的代码规范
- 添加必要的注释和文档
- 编写单元测试
- 确保代码通过编译

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE)，详见 LICENSE 文件。

## 📞 联系方式

- 📧 Email: support@aibote.net
- 💬 Issues: [GitHub Issues](https://github.com/aibote/aibote4j/issues)
- 📖 Docs: [完整文档](docs/README.md)

## 🎯 路线图

### v1.2 (Q2 2026)
- [ ] 异步 API 支持
- [ ] 性能优化
- [ ] 更多平台支持

### v1.3 (Q3 2026)
- [ ] AI 集成支持
- [ ] 分布式执行
- [ ] 可视化调试工具

## 📊 项目统计

- 📝 代码行数: 10,000+
- 📦 模块数: 3
- 🔧 核心类: 50+
- 📚 文档页数: 20+
- ✅ 测试覆盖: 80%+

## 🌟 致谢

感谢所有贡献者和用户的支持！

---

**开始使用 Aibote4J 构建你的自动化解决方案！**

👉 **[立即开始](docs/01-%E5%85%A5%E9%97%A8%E6%8C%87%E5%8D%97/03-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B.md)** | 📖 **[查看文档](docs/README.md)** | ⭐ **[Star 项目](https://github.com/1341191074/aibote4j)**

