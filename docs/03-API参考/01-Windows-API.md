# Windows API 参考

本章详细介绍 Windows 自动化相关的 API。

## WinBot 类

[WinBot](file:///F:/workspace/github/aibote4j/sdk-core/src/main/java/net/aibote/sdk/WinBot.java) 是 Windows 平台自动化的主要类。

## 窗口操作 API

### findWindow
查找窗口句柄

**方法签名：**
```java
public String findWindow(String className, String windowName)
```

**参数：**
- `className` - 窗口类名
- `windowName` - 窗口名称

**返回值：** 窗口句柄（String），查找失败返回 null

**示例：**
```java
WinBot bot = ...;
String hwnd = bot.findWindow("Notepad", "Untitled");
if (hwnd != null) {
    System.out.println("找到窗口: " + hwnd);
}
```

### findWindows
查找多个窗口句柄

**方法签名：**
```java
public String findWindows(String className, String windowName)
```

**返回值：** 以 "|" 分隔的窗口句柄数组

### findSubWindow
查找子窗口

**方法签名：**
```java
public String findSubWindow(String curHwnd, String className, String windowName)
```

**参数：**
- `curHwnd` - 当前窗口句柄
- `className` - 子窗口类名
- `windowName` - 子窗口名称

### findParentWindow
查找父窗口

**方法签名：**
```java
public String findParentWindow(String curHwnd)
```

### findDesktopWindow
查找桌面窗口

**方法签名：**
```java
public String findDesktopWindow()
```

### getWindowName
获取窗口名称

**方法签名：**
```java
public String getWindowName(String hwnd)
```

### showWindow
显示或隐藏窗口

**方法签名：**
```java
public boolean showWindow(String hwnd, boolean isShow)
```

**参数：**
- `hwnd` - 窗口句柄
- `isShow` - true 显示，false 隐藏

### setWindowTop
窗口置顶

**方法签名：**
```java
public boolean setWindowTop(String hwnd, boolean isTop)
```

### getWindowPos
获取窗口位置

**方法签名：**
```java
public String getWindowPos(String hwnd)
```

**返回值：** "left|top|right|bottom" 格式的字符串

### setWindowPos
设置窗口位置

**方法签名：**
```java
public boolean setWindowPos(String hwnd, int left, int top, int width, int height)
```

## 鼠标操作 API

### moveMouse
移动鼠标

**方法签名：**
```java
public boolean moveMouse(String hwnd, int x, int y, Mode mode, String elementHwnd)
```

**参数：**
- `hwnd` - 窗口句柄
- `x`, `y` - 目标坐标
- `mode` - 操作模式（前台/后台）
- `elementHwnd` - 元素句柄（可选）

**示例：**
```java
bot.moveMouse(hwnd, 500, 600, Mode.FOREGROUND, null);
```

### moveMouseRelative
相对移动鼠标

**方法签名：**
```java
public boolean moveMouseRelative(String hwnd, int x, int y, Mode mode)
```

### rollMouse
滚动鼠标

**方法签名：**
```java
public boolean rollMouse(String hwnd, int x, int y, int dwData, Mode mode)
```

**参数：**
- `dwData` - 正数上滚，负数下滚

### clickMouse
鼠标点击

**方法签名：**
```java
public boolean clickMouse(String hwnd, int x, int y, int mouseType, Mode mode, String elementHwnd)
```

**参数：**
- `mouseType` - 1:左单击, 2:右单击, 3:按下左键, 4:弹起左键, 5:按下右键, 6:弹起右键, 7:双击左键, 8:双击右键

**示例：**
```java
// 左击
bot.clickMouse(hwnd, 500, 600, 1, Mode.FOREGROUND, null);

// 右击
bot.clickMouse(hwnd, 500, 600, 2, Mode.FOREGROUND, null);

// 双击
bot.clickMouse(hwnd, 500, 600, 7, Mode.FOREGROUND, null);
```

## 键盘操作 API

### sendKeys
输入文本

**方法签名：**
```java
public boolean sendKeys(String txt)
```

**示例：**
```java
bot.sendKeys("Hello World");
```

### sendKeysByHwnd
后台输入文本

**方法签名：**
```java
public boolean sendKeysByHwnd(String hwnd, String txt)
```

### sendVk
输入虚拟键值

**方法签名：**
```java
public boolean sendVk(int vk, int keyState)
```

**参数：**
- `vk` - VK 键值（如 13 表示回车）
- `keyState` - 1:按下弹起, 2:按下, 3:弹起

**常用 VK 值：**
- 13 - 回车
- 27 - Esc
- 8 - 退格
- 9 - Tab
- 16 - Shift

**示例：**
```java
// 按回车键
bot.sendVk(13, 1);

// 按 Ctrl+A
bot.sendVk(17, 2);  // Ctrl 按下
bot.sendVk(65, 1);  // A 键按下弹起
bot.sendVk(17, 3);  // Ctrl 弹起
```

## 截图和图像 API

### saveScreenshot
保存截图

**方法签名：**
```java
public boolean saveScreenshot(String hwnd, String savePath, Region region, 
    int thresholdType, int thresh, int maxval)
```

**参数：**
- `hwnd` - 窗口句柄
- `savePath` - 保存路径
- `region` - 截图区域
- `thresholdType` - 算法类型（0-6）
- `thresh` - 阈值
- `maxval` - 最大值

**示例：**
```java
Region region = new Region(0, 0, 800, 600);
bot.saveScreenshot(hwnd, "screenshot.png", region, 0, 0, 0);
```

### getColor
获取像素颜色

**方法签名：**
```java
public String getColor(String hwnd, int x, int y, boolean mode)
```

**返回值：** "#RRGGBB" 格式的颜色值

### findImages
查找图片

**方法签名：**
```java
public String findImages(String hwndOrBigImagePath, String smallImagePath, 
    Region region, float sim, int thresholdType, int thresh, int maxval, int multi, Mode mode)
```

**参数：**
- `sim` - 相似度（0.0-1.0）
- `multi` - 找图数量

**示例：**
```java
String result = bot.findImages(hwnd, "target.png", region, 0.95f, 0, 0, 0, 1, Mode.FOREGROUND);
// 返回格式：x|y 或 x1|y1|x2|y2...
```

### findAnimation
查找动态图

**方法签名：**
```java
public String findAnimation(String hwnd, int frameRate, Region region, Mode mode)
```

### findColor
查找颜色

**方法签名：**
```java
public String findColor(String hwnd, String strMainColor, SubColor[] subColors, 
    Region region, float sim, Mode mode)
```

**示例：**
```java
SubColor[] subColors = {
    new SubColor(5, 5, "#FF0000"),
    new SubColor(-5, 5, "#00FF00")
};
String result = bot.findColor(hwnd, "#0000FF", subColors, region, 1.0f, Mode.FOREGROUND);
```

## OCR 文字识别 API

### initOcr
初始化 OCR 服务

**方法签名：**
```java
public boolean initOcr(String ocrServerIp, int ocrServerPort, boolean useAngleModel, 
    boolean enableGPU, boolean enableTensorrt)
```

### ocrByHwnd
从窗口识别文字

**方法签名：**
```java
public List<OCRResult> ocrByHwnd(String hwnd, Region region, int thresholdType, 
    int thresh, int maxval, Mode mode)
```

**返回值：** OCRResult 对象列表

**OCRResult 字段：**
```java
public class OCRResult {
    public Point lt;      // 左上角
    public Point rt;      // 右上角
    public Point ld;      // 左下角
    public Point rd;      // 右下角
    public String word;   // 识别的文字
    public float rate;    // 置信度
}
```

### ocrByFile
从文件识别文字

**方法签名：**
```java
public List<OCRResult> ocrByFile(String imagePath, Region region, int thresholdType, 
    int thresh, int maxval)
```

### getWords
获取屏幕文字

**方法签名：**
```java
public String getWords(String hwndOrImagePath, Region region, int thresholdType, 
    int thresh, int maxval, Mode mode)
```

### findWords
查找文字

**方法签名：**
```java
public Point findWords(String hwndOrImagePath, String word, Region region, int thresholdType, 
    int thresh, int maxval, Mode mode)
```

**示例：**
```java
List<OCRResult> results = bot.ocrByHwnd(hwnd, null, 0, 0, 0, Mode.FOREGROUND);
for (OCRResult result : results) {
    System.out.println("文字: " + result.word + ", 置信度: " + result.rate);
}
```

## YOLO 目标检测 API

### initYolo
初始化 YOLO 服务

**方法签名：**
```java
public boolean initYolo(String yoloServerIp, String modelPath, String classesPath)
```

### yoloByHwnd
从窗口进行目标检测

**方法签名：**
```java
public JSONArray yoloByHwnd(String hwnd, Mode mode)
```

**返回值：** JSON 数组，包含检测结果

### yoloByFile
从文件进行目标检测

**方法签名：**
```java
public JSONArray yoloByFile(String imagePath)
```

## UI 元素操作 API

### getElementName
获取元素名称

**方法签名：**
```java
public String getElementName(String hwnd, String xpath)
```

### getElementValue
获取元素文本

**方法签名：**
```java
public String getElementValue(String hwnd, String xpath)
```

### getElementRect
获取元素位置

**方法签名：**
```java
public Region getElementRect(String hwnd, String xpath)
```

**返回值：** Region 对象（包含 left, top, right, bottom）

### getElementWindow
获取元素窗口句柄

**方法签名：**
```java
public String getElementWindow(String hwnd, String xpath)
```

### clickElement
点击元素

**方法签名：**
```java
public boolean clickElement(String hwnd, String xpath, String opt)
```

**参数：**
- `opt` - 1:左击, 2:右击, 7:双击

### invokeElement
执行元素默认操作

**方法签名：**
```java
public boolean invokeElement(String hwnd, String xpath)
```

### setElementFocus
设置元素焦点

**方法签名：**
```java
public boolean setElementFocus(String hwnd, String xpath)
```

### setElementValue
设置元素文本

**方法签名：**
```java
public boolean setElementValue(String hwnd, String xpath, String value)
```

### setElementScroll
滚动元素

**方法签名：**
```java
public boolean setElementScroll(String hwnd, String xpath, float horizontalPercent, float verticalPercent)
```

### isSelected
检查元素是否选中

**方法签名：**
```java
public boolean isSelected(String hwnd, String xpath)
```

### closeWindow
关闭窗口

**方法签名：**
```java
public boolean closeWindow(String hwnd, String xpath)
```

### setWindowState
设置窗口状态

**方法签名：**
```java
public boolean setWindowState(String hwnd, String xpath, int state)
```

**参数：** state - 0:正常, 1:最大化, 2:最小化

## 系统操作 API

### setClipboardText
设置剪贴板文本

**方法签名：**
```java
public boolean setClipboardText(String text)
```

### getClipboardText
获取剪贴板文本

**方法签名：**
```java
public String getClipboardText()
```

### startProcess
启动程序

**方法签名：**
```java
public boolean startProcess(String commandLine, boolean showWindow, boolean isWait)
```

### executeCommand
执行 CMD 命令

**方法签名：**
```java
public String executeCommand(String command, int waitTimeout)
```

**示例：**
```java
String result = bot.executeCommand("dir C:\\", 300);
System.out.println(result);
```

### downloadFile
下载文件

**方法签名：**
```java
public boolean downloadFile(String url, String filePath, boolean isWait)
```

## Excel 操作 API

### openExcel
打开 Excel 文件

**方法签名：**
```java
public JSONObject openExcel(String excelPath)
```

### openExcelSheet
打开 Excel 表单

**方法签名：**
```java
public JSONObject openExcelSheet(JSONObject excelObject, String sheetName)
```

### saveExcel
保存 Excel 文件

**方法签名：**
```java
public boolean saveExcel(JSONObject excelObject)
```

### writeExcelNum
写入数字

**方法签名：**
```java
public boolean writeExcelNum(JSONObject sheetObject, int row, int col, int value)
```

### writeExcelStr
写入字符串

**方法签名：**
```java
public boolean writeExcelStr(JSONObject sheetObject, int row, int col, String strValue)
```

### readExcelNum
读取数字

**方法签名：**
```java
public Float readExcelNum(JSONObject sheetObject, int row, int col)
```

### readExcelStr
读取字符串

**方法签名：**
```java
public String readExcelStr(JSONObject sheetObject, int row, int col)
```

### removeExcelRow
删除行

**方法签名：**
```java
public boolean removeExcelRow(JSONObject sheetObject, int rowFirst, int rowLast)
```

### removeExcelCol
删除列

**方法签名：**
```java
public boolean removeExcelCol(JSONObject sheetObject, int rowFirst, int rowLast)
```

## 验证码识别 API

### getCaptcha
识别验证码

**方法签名：**
```java
public JSONObject getCaptcha(String filePath, String username, String password, 
    String softId, String codeType, String lenMin)
```

**返回值：** 包含 err_no, err_str, pic_id, pic_str, md5 的 JSON 对象

### errorCaptcha
报错返分

**方法签名：**
```java
public JSONObject errorCaptcha(String username, String password, String softId, String picId)
```

### scoreCaptcha
查询剩余题分

**方法签名：**
```java
public JSONObject scoreCaptcha(String username, String password)
```

**示例：**
```java
JSONObject result = bot.getCaptcha("captcha.png", "user", "pass", "softid", "1902", 0);
if (result.getInteger("err_no") == 0) {
    System.out.println("验证码: " + result.getString("pic_str"));
}
```