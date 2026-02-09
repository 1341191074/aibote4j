# Android 自动化

Android 自动化功能通过 [AndroidBot](file:///F:/workspace/github/aibote4j/sdk-core/src/main/java/net/aibote/sdk/AndroidBot.java) 类提供，支持对 Android 设备进行全面的自动化操作。

## 🚀 快速开始

### 创建 Android 机器人

> ⚠️ **重要提醒**：使用前必须先启动服务端！
> 详细启动顺序说明请参考：[启动顺序说明](../01-入门指南/04-启动顺序说明.md)

```java
import net.aibote.sdk.AndroidBot;
import net.aibote.sdk.factory.BotFactory;

// 创建 Android 机器人
AndroidBot androidBot = BotFactory.builder()
    .withBotType(BotFactory.BotType.ANDROID)
    .build();
```

### 连接到设备

```java
// 通过任务引擎使用Android自动化
NotepadAutomationTask task = NotepadAutomationTask.builder()
    .taskName("Android自动化任务")
    .scriptName("Android-Auto")
    .build();

TaskEngine.getInstance().registerTask("android-task", task);
Application.main(new String[]{}); // 启动服务端（第一步）

// 客户端需要在另一个进程启动AndroidDriver.exe（第二步）
// 客户端连接端口16997
```

## 📱 基本操作

### 设备信息

```java
// 获取设备 ID
String deviceId = androidBot.getAndroidId();

// 获取脚本名称
String scriptName = androidBot.getScriptName();

// 休眠
androidBot.sleep(1000); // 休眠 1 秒
```

### 屏幕信息

```java
// 获取屏幕大小
String screenSize = androidBot.getWindowSize(); // 格式："width|height"

// 获取屏幕旋转角度
int rotation = androidBot.getRotationAngle(); // 0, 90, 180, 270
```

## 🖱️ 触摸操作

### 基本触摸

```java
// 点击
androidBot.click(500, 1000); // x, y 坐标

// 长按
androidBot.longClick(500, 1000, 2000); // x, y, 持续时间(毫秒)

// 滑动
androidBot.swipe(500, 1500, 500, 500, 1000); // startX, startY, endX, endY, 持续时间
```

### 多点手势

```java
// 执行复杂手势
androidBot.dispatchGestures("gesture.json"); // 从文件加载手势路径
```

## ⌨️ 文本输入

### 文本操作

```java
// 输入文本
androidBot.typeText("Hello Android!");

// 清空文本（通过退格键）
androidBot.clearText(10); // 删除 10 个字符
```

## 📷 截图和图像识别

### 截图操作

```java
// 截取屏幕
Region region = new Region(0, 0, 1080, 1920);
byte[] screenshot = androidBot.takeScreenshot(region, 0, 0, 0, 1.0f);

// 截图参数说明：
// region: 截图区域
// thresholdType: 算法类型
// thresh: 阈值
// maxval: 最大值
// scale: 缩放比例
```

### 图像查找

```java
// 查找单个图片
Point imagePos = androidBot.findImage("target.png", 0.9f); // 图片路径，相似度

// 查找多个图片
List<Point> imagePositions = androidBot.findImages("target.png", 0.9f, 5); // 图片路径，相似度，最大数量
```

## 📝 OCR 识别

### OCR 操作

```java
// 识别区域内文字
List<OCRResult> ocrResults = androidBot.ocr(region, 0, 0, 0, 1.0f);

// 获取屏幕文字
String screenText = androidBot.ocrText(region);
```

## 🧩 UI 元素操作

### 元素查询

```java
String xpath = "//android.widget.Button[@text='确定']";

// 获取所有元素
JSONObject elements = androidBot.getElements();

// 获取元素位置
Region elementRect = androidBot.getElementRect(xpath);

// 获取元素文本
String elementText = androidBot.getElementText(xpath);

// 检查元素是否存在
boolean exists = androidBot.existsElement(xpath);

// 检查元素是否选中
boolean selected = androidBot.isSelectedElement(xpath);

// 检查元素是否启用
boolean enabled = androidBot.isEnabledElement(xpath);
```

### 元素操作

```java
// 设置元素文本
androidBot.setElementText(xpath, "New Text");

// 点击元素
androidBot.clickElement(xpath);
```

## ⚙️ HID 硬件控制

### HID 操作

```java
// HID 按下
androidBot.hidPress(500, 1000); // x, y

// HID 移动
androidBot.hidMove(500, 1000, 500); // x, y, 持续时间

// HID 点击
androidBot.hidClick(500, 1000); // x, y

// HID 滑动
androidBot.hidSwipe(500, 1500, 500, 500, 1000); // startX, startY, endX, endY, 持续时间

// HID 手势
androidBot.hidDispatchGesture("gesture.json", 1000); // 手势文件路径，持续时间

// 系统按键
androidBot.hidBack(); // 返回键
androidBot.hidHome(); // Home 键
```

## 🔢 验证码识别

### 验证码处理

```java
// 识别验证码
JSONObject captchaResult = androidBot.getCaptcha(
    "captcha.png",      // 文件路径
    "username",         // 用户名
    "password",         // 密码
    "softId",           // 软件ID
    "1902",             // 验证码类型
    0                   // 最小长度
);

// 错误反馈
JSONObject errorResult = androidBot.errorCaptcha("username", "password", "softId", "picId");

// 查询余额
JSONObject scoreResult = androidBot.scoreCaptcha("username", "password");

// 处理结果
if (captchaResult.getInteger("err_no") == 0) {
    String captchaText = captchaResult.getString("pic_str");
    System.out.println("验证码: " + captchaText);
}
```

## 🧪 实际应用示例

### 自动化登录示例

```java
public class LoginAutomation extends AndroidBot {
    @Override
    public String getScriptName() {
        return "LoginAutomation";
    }

    @Override
    public void doScript() {
        try {
            // 点击登录按钮
            Point loginBtn = this.findImage("login_button.png", 0.9f);
            if (loginBtn != null) {
                this.click(loginBtn.x, loginBtn.y);
                this.sleep(2000);
                
                // 输入用户名
                this.click(500, 800); // 用户名输入框位置
                this.typeText("username123");
                
                // 输入密码
                this.click(500, 1000); // 密码输入框位置
                this.typeText("password123");
                
                // 点击确认
                this.click(500, 1200); // 登录按钮位置
            }
        } catch (Exception e) {
            System.err.println("自动化登录失败: " + e.getMessage());
        }
    }
}
```

### 自动化滑动示例

```java
public void autoScroll() {
    int screenHeight = Integer.parseInt(this.getWindowSize().split("\\|")[1]);
    
    // 循环滑动
    for (int i = 0; i < 5; i++) {
        // 从屏幕下半部分滑动到上半部分
        this.swipe(500, screenHeight - 200, 500, 200, 1000);
        this.sleep(2000); // 等待内容加载
    }
}
```

## 💡 最佳实践

### 连接管理

```java
// 正确的任务驱动方式
public class AndroidAutomationTask implements TaskDefinition {
    
    @Override
    public void executeTask(AbstractPlatformBot bot) throws Exception {
        if (!(bot instanceof AndroidBot)) {
            throw new IllegalArgumentException("仅支持Android机器人");
        }
        
        AndroidBot androidBot = (AndroidBot) bot;
        
        // 执行自动化操作
        Point target = androidBot.findImage("target.png", 0.9f);
        if (target != null) {
            androidBot.click(target.x, target.y);
        }
    }
    
    @Override
    public Set<BotFactory.BotType> getSupportedBotTypes() {
        return Set.of(BotFactory.BotType.ANDROID);
    }
}
```

### 错误处理

```java
try {
    // 尝试查找图片
    Point target = androidBot.findImage("target.png", 0.9f);
    if (target != null) {
        androidBot.click(target.x, target.y);
    } else {
        System.out.println("未找到目标图片");
    }
} catch (CommandException e) {
    System.err.println("命令执行失败: " + e.getErrorMessage());
} catch (TimeoutException e) {
    System.err.println("操作超时: " + e.getErrorMessage());
}
```

### 性能优化

- 使用合适的相似度阈值以平衡准确性和性能
- 合理设置查找区域以减少搜索时间
- 对于频繁操作，考虑使用缓存机制
- 避免过于频繁的截图操作，影响性能
- 在不需要精确控制时，优先使用 XPath 而非图像识别

## 🚨 注意事项

- 确保 Android 设备已开启 USB 调试模式
- 确保 ADB 工具已正确安装并配置
- 部分操作可能需要设备 Root 权限
- 注意遵守应用的使用条款，避免违规操作