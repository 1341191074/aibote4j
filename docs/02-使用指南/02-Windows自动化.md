# Windows 自动化

Windows 自动化功能通过 [WinBot](file:///F:/workspace/github/aibote4j/sdk-core/src/main/java/net/aibote/sdk/WinBot.java) 类提供，支持对 Windows 桌面应用程序进行全面的自动化操作。

## 🚀 快速开始

### 创建 Windows 机器人

```java
import net.aibote.sdk.WinBot;
import net.aibote.sdk.factory.BotFactory;

// 创建 Windows 机器人
WinBot winBot = BotFactory.builder()
    .withBotType(BotFactory.BotType.WIN)
    .build();
```

### 连接到设备

```java
// 连接到 Windows Driver
if (winBot.connect()) {
    System.out.println("连接成功！");
} else {
    System.out.println("连接失败！");
}
```

## 🖥️ 窗口操作

### 查找窗口

```java
// 查找窗口句柄
String hwnd = winBot.findWindow("Notepad", "无标题 - 记事本");

// 查找多个窗口
String hwnds = winBot.findWindows("Notepad", null);

// 查找子窗口
String subHwnd = winBot.findSubWindow(hwnd, "Edit", null);
```

### 窗口管理

```java
// 获取窗口名称
String windowName = winBot.getWindowName(hwnd);

// 显示/隐藏窗口
winBot.showWindow(hwnd, true); // 显示
winBot.showWindow(hwnd, false); // 隐藏

// 窗口置顶
winBot.setWindowTop(hwnd, true);

// 获取窗口位置
String pos = winBot.getWindowPos(hwnd); // 格式："left|top|right|bottom"

// 设置窗口位置
winBot.setWindowPos(hwnd, 100, 100, 800, 600); // left, top, width, height
```

## 🖱️ 鼠标操作

### 基本鼠标操作

```java
// 移动鼠标
winBot.moveMouse(hwnd, 100, 100, Mode.FOREGROUND, null);

// 相对移动
winBot.moveMouseRelative(hwnd, 50, 50, Mode.FOREGROUND);

// 点击鼠标
winBot.clickMouse(hwnd, 100, 100, 1, Mode.FOREGROUND, null); // 左键单击
winBot.clickMouse(hwnd, 100, 100, 2, Mode.FOREGROUND, null); // 右键单击
winBot.clickMouse(hwnd, 100, 100, 7, Mode.FOREGROUND, null); // 左键双击

// 滚动鼠标
winBot.rollMouse(hwnd, 100, 100, 120, Mode.FOREGROUND); // 向上滚动
winBot.rollMouse(hwnd, 100, 100, -120, Mode.FOREGROUND); // 向下滚动
```

## ⌨️ 键盘操作

### 文本输入

```java
// 输入文本
winBot.sendKeys("Hello World!");

// 后台输入
winBot.sendKeysByHwnd(hwnd, "Background input");

// 发送虚拟键值
winBot.sendVk(13, 1); // 回车键
winBot.sendVk(8, 1);  // 退格键
winBot.sendVk(9, 1);  // Tab键
```

### 常用虚拟键值

| 键值 | 键名 | 说明 |
|------|------|------|
| 8 | VK_BACK | 退格键 |
| 9 | VK_TAB | Tab键 |
| 13 | VK_RETURN | 回车键 |
| 16 | VK_SHIFT | Shift键 |
| 17 | VK_CONTROL | Ctrl键 |
| 18 | VK_ALT | Alt键 |
| 27 | VK_ESCAPE | Esc键 |
| 32 | VK_SPACE | 空格键 |
| 37-40 | 方向键 | 左、上、右、下 |
| 46 | VK_DELETE | Delete键 |

## 📷 截图和图像识别

### 截图操作

```java
// 保存窗口截图
Region region = new Region(0, 0, 800, 600);
winBot.saveScreenshot(hwnd, "screenshot.png", region, 0, 0, 0);

// 获取像素颜色
String color = winBot.getColor(hwnd, 100, 100, true); // 格式:"#RRGGBB"
```

### 图像查找

```java
// 查找图片
String result = winBot.findImages(hwnd, "target.png", region, 0.95f, 
    0, 0, 0, 1, Mode.FOREGROUND);
// 返回格式：x|y 或 x1|y1|x2|y2...

// 查找颜色
SubColor[] subColors = {
    new SubColor(5, 5, "#FF0000"),
    new SubColor(-5, 5, "#00FF00")
};
String colorResult = winBot.findColor(hwnd, "#0000FF", subColors, 
    region, 1.0f, Mode.FOREGROUND);
```

## 📝 OCR 文字识别

### 初始化 OCR

```java
// 初始化 OCR 服务
winBot.initOcr("127.0.0.1", 9898, true, false, false);
```

### 文字识别

```java
// 从窗口识别文字
List<OCRResult> results = winBot.ocrByHwnd(hwnd, region, 0, 0, 0, Mode.FOREGROUND);

// 从文件识别文字
List<OCRResult> fileResults = winBot.ocrByFile("image.png", region, 0, 0, 0);

// 获取屏幕文字
String words = winBot.getWords(hwnd, region, 0, 0, 0, Mode.FOREGROUND);

// 查找特定文字
Point wordPos = winBot.findWords(hwnd, "Hello", region, 0, 0, 0, Mode.FOREGROUND);

// OCR 结果结构
for (OCRResult result : results) {
    System.out.println("文字: " + result.word);
    System.out.println("置信度: " + result.rate);
    System.out.println("位置: " + result.lt.x + "," + result.lt.y); // 左上角
}
```

## 🎯 YOLO 目标检测

### 初始化 YOLO

```java
// 初始化 YOLO 服务
winBot.initYolo("127.0.0.1", "model.onnx", "classes.txt");
```

### 目标检测

```java
// 从窗口进行目标检测
JSONArray detections = winBot.yoloByHwnd(hwnd, Mode.FOREGROUND);

// 从文件进行目标检测
JSONArray fileDetections = winBot.yoloByFile("image.png");
```

## 🧩 UI 元素操作

### 元素信息获取

```java
String xpath = "//button[@id='submit']";

// 获取元素名称
String name = winBot.getElementName(hwnd, xpath);

// 获取元素文本
String value = winBot.getElementValue(hwnd, xpath);

// 获取元素位置
Region rect = winBot.getElementRect(hwnd, xpath);

// 获取元素窗口句柄
String elementHwnd = winBot.getElementWindow(hwnd, xpath);
```

### 元素操作

```java
// 点击元素
winBot.clickElement(hwnd, xpath, "1"); // 1:左击, 2:右击, 7:双击

// 执行元素默认操作
winBot.invokeElement(hwnd, xpath);

// 设置元素焦点
winBot.setElementFocus(hwnd, xpath);

// 设置元素文本
winBot.setElementValue(hwnd, xpath, "New text");

// 滚动元素
winBot.setElementScroll(hwnd, xpath, 0.5f, 0.8f); // 水平50%，垂直80%

// 检查元素是否选中
boolean isSelected = winBot.isSelected(hwnd, xpath);

// 关闭窗口
winBot.closeWindow(hwnd, xpath);

// 设置窗口状态
winBot.setWindowState(hwnd, xpath, 1); // 0:正常, 1:最大化, 2:最小化
```

## 🧰 系统操作

### 剪贴板操作

```java
// 设置剪贴板文本
winBot.setClipboardText("Hello Clipboard");

// 获取剪贴板文本
String clipboardText = winBot.getClipboardText();
```

### 程序控制

```java
// 启动程序
winBot.startProcess("notepad.exe", true, false);

// 执行 CMD 命令
String cmdResult = winBot.executeCommand("dir C:\\", 30000); // 30秒超时
```

### 文件操作

```java
// 下载文件
winBot.downloadFile("http://example.com/file.zip", "local_file.zip", true);
```

## 📊 Excel 操作

### Excel 文件操作

```java
// 打开 Excel 文件
JSONObject excelObj = winBot.openExcel("data.xlsx");

// 打开工作表
JSONObject sheetObj = winBot.openExcelSheet(excelObj, "Sheet1");

// 保存 Excel 文件
winBot.saveExcel(excelObj);
```

### 数据读写

```java
// 写入数字
winBot.writeExcelNum(sheetObj, 1, 1, 123);

// 写入字符串
winBot.writeExcelStr(sheetObj, 1, 2, "Hello");

// 读取数字
Float numValue = winBot.readExcelNum(sheetObj, 1, 1);

// 读取字符串
String strValue = winBot.readExcelStr(sheetObj, 1, 2);
```

### 行列操作

```java
// 删除行
winBot.removeExcelRow(sheetObj, 1, 5); // 删除第1到第5行

// 删除列
winBot.removeExcelCol(sheetObj, 1, 2); // 删除第1到第2列
```

## 🔢 验证码识别

### 验证码处理

```java
// 识别验证码
JSONObject captchaResult = winBot.getCaptcha(
    "captcha.png",      // 文件路径
    "username",         // 用户名
    "password",         // 密码
    "softId",           // 软件ID
    "1902",             // 验证码类型
    0                   // 最小长度
);

// 错误反馈
JSONObject errorResult = winBot.errorCaptcha("username", "password", "softId", "picId");

// 查询余额
JSONObject scoreResult = winBot.scoreCaptcha("username", "password");

// 处理结果
if (captchaResult.getInteger("err_no") == 0) {
    String captchaText = captchaResult.getString("pic_str");
    System.out.println("验证码: " + captchaText);
}
```

## 💡 最佳实践

### 连接管理

```java
// 确保正确管理连接
try {
    WinBot bot = BotFactory.builder()
        .withBotType(BotFactory.BotType.WIN)
        .build();
    
    if (bot.connect()) {
        // 执行操作
        bot.clickMouse(hwnd, 100, 100, 1, Mode.FOREGROUND, null);
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
    String hwnd = winBot.findWindow("Notepad", null);
    if (hwnd != null) {
        winBot.clickMouse(hwnd, 100, 100, 1, Mode.FOREGROUND, null);
    } else {
        System.out.println("未找到目标窗口");
    }
} catch (CommandException e) {
    System.err.println("命令执行失败: " + e.getErrorMessage());
} catch (TimeoutException e) {
    System.err.println("操作超时: " + e.getErrorMessage());
}
```

### 性能优化

- 使用合适的区域参数限制搜索范围
- 合理设置相似度阈值以平衡准确性和性能
- 对于频繁操作，考虑使用缓存机制
- 避免过于频繁的截图操作