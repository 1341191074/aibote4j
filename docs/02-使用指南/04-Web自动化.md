# Web 自动化

Web 自动化功能通过 [WebBot](file:///F:/workspace/github/aibote4j/sdk-core/src/main/java/net/aibote/sdk/WebBot.java) 类提供，支持对 Web 应用程序进行全面的自动化操作。

## 🚀 快速开始

### 创建 Web 机器人

> ⚠️ **重要提醒**：使用前必须先启动服务端！
> 详细启动顺序说明请参考：[启动顺序说明](../01-入门指南/04-启动顺序说明.md)

```java
import net.aibote.sdk.WebBot;
import net.aibote.sdk.factory.BotFactory;

// 创建 Web 机器人
WebBot webBot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WEB)
    .build();
```

### 启动浏览器客户端

```java
// 首先需要启动 WebDriver 客户端（参考 ClientDriverStartupExample）
// 注意：这应该在创建 WebBot 实例之前执行

// 方式1：启动 Web 客户端驱动
ClientDriverStartupExample.startWebClientDriver(
    "127.0.0.1",     // serverIp
    "16998",         // serverPort  
    "chrome",        // browserName
    null,             // browserPath (可选)
    "0",             // debugPort (0表示自动分配)
    null,             // argument (可选)
    null,             // userDataDir (可选)
    null              // webDriverPath (可选)
);

// 方式2：或者使用更简洁的方式
String serverIp = "127.0.0.1";
String serverPort = "16998";
String browserName = "chrome";
ClientDriverStartupExample.startWebClientDriver(serverIp, serverPort, browserName, null, "0", null, null, null);

// 然后创建 WebBot 实例并连接
WebBot webBot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WEB)
    .build();

// 导航到网页
webBot.navigate("https://www.example.com");
```

## 🌐 浏览器操作

### 页面导航

```java
// 跳转到指定 URL
webBot.navigate("https://www.example.com");

// 获取当前 URL
String currentUrl = webBot.getCurrentUrl();

// 返回上一页
webBot.goBack();

// 前进到下一页
webBot.goForward();

// 刷新页面
webBot.refresh();
```

### 标签页管理

```java
// 新建标签页并导航
webBot.newPage("https://www.another-site.com");

// 获取当前页面 ID
String currentPageId = webBot.getCurPageId();

// 获取所有页面 ID
String[] pageIds = webBot.getAllPageId();

// 切换页面
webBot.switchPage("page-id");

// 关闭当前页面
webBot.closePage();
```

## 🧩 元素操作

### 元素定位

```java
String xpath = "//input[@id='username']";

// 点击元素
webBot.clickElement(xpath);

// 设置元素值
webBot.setElementValue(xpath, "myusername");

// 获取元素值
String value = webBot.getElementValue(xpath);

// 获取元素文本
String text = webBot.getElementText(xpath);

// 获取元素内容
String content = webBot.getElementContent(xpath);

// 获取 outerHTML
String outerHtml = webBot.getElementOuterHTML(xpath);

// 获取 innerHTML
String innerHtml = webBot.getElementInnerHTML(xpath);

// 获取元素矩形位置
Region rect = webBot.getElementRect(xpath);

// 设置元素属性
webBot.setElementAttribute(xpath, "value", "newValue");

// 获取元素属性
String attrValue = webBot.getElementAttribute(xpath, "placeholder");
```

### 元素状态检查

```java
// 检查元素是否选中
boolean isSelected = webBot.isSelected(xpath);

// 检查元素是否可见
boolean isDisplayed = webBot.isDisplayed(xpath);

// 检查元素是否可用
boolean isEnabled = webBot.isEnabled(xpath);

// 清空元素值
webBot.clearElement(xpath);

// 设置元素焦点
webBot.setElementFocus(xpath);
```

## 🖱️ 鼠标操作

### 基本鼠标操作

```java
// 点击指定坐标
webBot.clickMouse(500, 600, 1); // x, y, 消息类型(1:左键单击)

// 移动鼠标
webBot.moveMouse(500, 600); // x, y

// 滚动鼠标
webBot.wheelMouse(0, -100); // deltaX, deltaY (垂直向下滚动)

// 指定坐标滚动
webBot.wheelMouse(50, -100, 300, 400); // deltaX, deltaY, x, y

// 通过 XPath 点击
webBot.clickMouseByXpath(xpath, 1); // xpath, 消息类型

// 通过 XPath 移动鼠标到元素中心
webBot.moveMouseByXpath(xpath);

// 通过 XPath 滚动
webBot.wheelMouseByXpath(xpath, 0, -100); // xpath, deltaX, deltaY
```

## ⌨️ 键盘操作

### 文本输入

```java
// 在指定元素输入文本
webBot.sendKeys(xpath, "Hello Web!");

// 发送虚拟键
webBot.sendVk(13); // 发送回车键 (VK_ENTER)
webBot.sendVk(8);  // 发送退格键 (VK_BACK)
webBot.sendVk(9);  // 发送制表键 (VK_TAB)
webBot.sendVk(37); // 发送方向左键 (VK_LEFT)
webBot.sendVk(38); // 发送方向上键 (VK_UP)
webBot.sendVk(39); // 发送方向右键 (VK_RIGHT)
webBot.sendVk(40); // 发送方向下键 (VK_DOWN)
webBot.sendVk(46); // 发送删除键 (VK_DELETE)
```

## 🖼️ 触摸操作

### 触摸事件模拟

```java
// 开始触屏
webBot.touchStart(500, 600); // x, y

// 移动触屏
webBot.touchMove(550, 650); // x, y

// 结束触屏
webBot.touchEnd(550, 650); // x, y
```

## 📄 页面操作

### 获取页面信息

```java
// 获取页面标题
String title = webBot.getTitle();

// 获取所有元素信息
JSONObject elements = webBot.getElements();

// 显示元素 XPath 路径（用于调试）
webBot.showXpath(); // 调用后可以在页面上查看元素路径
```

### 文件上传

```java
// 通过元素上传文件
webBot.uploadFile("//input[@type='file']", "/path/to/local/file.txt");
```

## 🧪 JavaScript 执行

### 执行脚本

```java
// 执行 JavaScript
Object result = webBot.executeScript("return document.title;");

// 执行带参数的脚本
Object scriptResult = webBot.executeScript(
    "arguments[0].style.border='3px solid red'", 
    elementReference
);
```

## 🍪 Cookie 管理

### Cookie 操作

```java
// 添加 Cookie
webBot.addCookie("session_id", "abc123");

// 删除 Cookie
webBot.deleteCookie("session_id");

// 获取所有 Cookies
String allCookies = webBot.getAllCookies();
```

## 📷 截图操作

### 页面截图

```java
// 截取整个页面
String screenshotBase64 = webBot.takeScreenshot();

// 截取特定元素
String elementScreenshot = webBot.takeScreenshot("//div[@id='target']");
```

## 🔄 框架操作

### iframe 切换

```java
// 切换到指定 iframe
webBot.switchFrame("//iframe[@id='myFrame']");

// 切换回主框架
webBot.switchMainFrame();
```

## ⏱️ 隐式等待

### 设置等待策略

```java
// 设置隐式等待时间
webBot.setImplicitTimeout(10000); // 等待 10 秒

// 设置心跳间隔
webBot.setImplicitTimeout(10000, 500); // 等待 10 秒，心跳间隔 500 毫秒
```

## 🧪 实际应用示例

### 完整的 Web 自动化流程示例

```java
import net.aibote.examples.ClientDriverStartupExample;
import net.aibote.sdk.WebBot;
import net.aibote.sdk.factory.BotFactory;

public class WebAutomationExample {
    
    public static void main(String[] args) {
        WebBot webBot = null;
        try {
            // 1. 启动 WebDriver 客户端
            System.out.println("启动 WebDriver 客户端...");
            ClientDriverStartupExample.startWebClientDriver(
                "127.0.0.1",    // serverIp
                "16998",        // serverPort
                "chrome",       // browserName
                null,           // browserPath
                "0",            // debugPort
                null,           // argument
                null,           // userDataDir
                null            // webDriverPath
            );
            
            // 等待客户端启动
            Thread.sleep(2000);
            
            // 2. 创建 WebBot 实例
            System.out.println("创建 WebBot 实例...");
            webBot = BotFactory.builder()
                .withBotType(BotFactory.BotType.WEB)
                .build();
            
            // 3. 执行自动化操作
            performWebAutomation(webBot);
            
        } catch (Exception e) {
            System.err.println("Web 自动化执行失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 4. 清理资源
            cleanup(webBot);
        }
    }
    
    private static void performWebAutomation(WebBot bot) {
        try {
            System.out.println("开始执行 Web 自动化...");
            
            // 导航到目标网站
            bot.navigate("https://httpbin.org/forms/post");
            System.out.println("已导航到表单页面");
            
            // 填写表单
            bot.setElementValue("//input[@name='custname']", "张三");
            bot.setElementValue("//input[@name='custtel']", "13800138000");
            bot.setElementValue("//input[@name='custemail']", "zhangsan@example.com");
            
            // 选择单选按钮
            bot.clickElement("//input[@id='sizeLarge']");
            
            // 选择复选框
            bot.clickElement("//input[@name='topping' and @value='bacon']");
            bot.clickElement("//input[@name='topping' and @value='cheese']");
            
            // 填写备注
            bot.setElementValue("//textarea[@name='comments']", "这是一个测试订单");
            
            // 提交表单
            bot.clickElement("//input[@value='Submit']");
            
            System.out.println("表单提交完成");
            
            // 等待结果页面加载
            Thread.sleep(3000);
            
            // 截图保存结果
            String screenshot = bot.takeScreenshot();
            System.out.println("页面截图已获取，长度: " + screenshot.length());
            
        } catch (Exception e) {
            System.err.println("执行自动化操作时出错: " + e.getMessage());
        }
    }
    
    private static void cleanup(WebBot bot) {
        if (bot != null) {
            try {
                System.out.println("清理资源...");
                bot.closeBrowser();
                bot.closeDriver();
                bot.disconnect();
                System.out.println("资源清理完成");
            } catch (Exception e) {
                System.err.println("清理资源时出错: " + e.getMessage());
            }
        }
    }
}
```

### 登录自动化示例

```java
public class WebLoginAutomation extends WebBot {
    @Override
    public String getScriptName() {
        return "WebLoginAutomation";
    }

    @Override
    public void doScript() {
        try {
            // 导航到登录页面
            this.navigate("https://example.com/login");
            
            // 输入用户名
            this.setElementValue("//input[@id='username']", "myuser");
            
            // 输入密码
            this.setElementValue("//input[@id='password']", "mypassword");
            
            // 点击登录按钮
            this.clickElement("//button[@type='submit']");
            
            // 等待页面跳转
            this.sleep(3000);
            
            // 验证登录成功
            String title = this.getTitle();
            if (title.contains("Dashboard")) {
                System.out.println("登录成功");
            } else {
                System.out.println("登录可能失败");
            }
        } catch (Exception e) {
            System.err.println("自动化登录失败: " + e.getMessage());
        }
    }
}
```

### 表单填写示例

```java
public void fillContactForm() {
    try {
        // 填写姓名
        this.setElementValue("//input[@name='name']", "John Doe");
        
        // 填写邮箱
        this.setElementValue("//input[@name='email']", "john@example.com");
        
        // 填写电话
        this.setElementValue("//input[@name='phone']", "+1-234-567-8900");
        
        // 选择国家
        this.setElementValue("//select[@name='country']", "US");
        
        // 填写消息
        this.setElementValue("//textarea[@name='message']", "Hello, this is a test message.");
        
        // 勾选同意条款
        this.clickElement("//input[@name='agree']");
        
        // 提交表单
        this.clickElement("//button[@type='submit']");
        
        System.out.println("表单填写并提交成功");
    } catch (Exception e) {
        System.err.println("表单填写失败: " + e.getMessage());
    }
}
```

## 💡 最佳实践

### 连接管理

```java
// 确保正确管理连接
try {
    WebBot bot = BotFactory.builder()
        .withBotType(BotFactory.BotType.WEB)
        .build();
    
    // 通过任务引擎执行Web自动化
    NotepadAutomationTask task = NotepadAutomationTask.builder()
        .taskName("Web自动化任务")
        .scriptName("Web-Auto")
        .build();
    
    TaskEngine.getInstance().registerTask("web-task", task);
    Application.main(new String[]{});
} finally {
    // 确保断开连接
    if (bot != null) {
        bot.disconnect();
    }
}
```

### 错误处理

```java
try {
    // 等待元素出现并操作
    boolean clicked = this.clickElement("//button[@id='submit']");
    if (!clicked) {
        System.out.println("未能点击提交按钮");
    }
} catch (CommandException e) {
    System.err.println("命令执行失败: " + e.getErrorMessage());
} catch (TimeoutException e) {
    System.err.println("操作超时: " + e.getErrorMessage());
}
```

### 性能优化

- 使用显式等待而非固定延时
- 优先使用 ID 定位器而非 XPath
- 合理使用隐式等待减少超时
- 避免频繁的页面截图操作
- 在复杂的动态页面中使用适当的等待策略

## 🚨 注意事项

- 确保 WebDriver 服务已启动
- 确保浏览器驱动与浏览器版本兼容
- 部分网站可能有反自动化检测机制
- 注意遵守网站的使用条款和服务协议
- 考虑添加适当的延时以模拟真实用户行为