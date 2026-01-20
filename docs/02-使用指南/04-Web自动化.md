# Web 自动化

Web 自动化功能通过 [WebBot](file:///F:/workspace/github/aibote4j/sdk-core/src/main/java/net/aibote/sdk/WebBot.java) 类提供，支持对 Web 应用程序进行全面的自动化操作。

## 🚀 快速开始

### 创建 Web 机器人

```java
import net.aibote.sdk.WebBot;
import net.aibote.sdk.factory.BotFactory;

// 创建 Web 机器人
WebBot webBot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WEB)
    .build();
```

### 启动浏览器

```java
// 启动 Chrome 浏览器
webBot.startChrome("path/to/chromedriver", "path/to/profile");

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
    
    if (bot.connect()) {
        // 执行操作
        bot.navigate("https://www.example.com");
    }
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