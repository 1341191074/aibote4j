# Android API 参考

本章详细介绍 Android 自动化相关的 API。

## AndroidBot 类

[AndroidBot](file:///F:/workspace/github/aibote4j/sdk-core/src/main/java/net/aibote/sdk/AndroidBot.java) 是 Android 平台自动化的主要类。

## 基本操作 API

### getAndroidId
获取设备 ID

**方法签名：**
```java
public String getAndroidId()
```

**返回值：** 设备 ID 字符串

### getScriptName
获取脚本名称

**方法签名：**
```java
public String getScriptName()
```

**返回值：** 脚本名称

### sleep
休眠

**方法签名：**
```java
public void sleep(int milliseconds)
```

**参数：**
- `milliseconds` - 休眠时间（毫秒）

## 屏幕操作 API

### takeScreenshot
截图

**方法签名：**
```java
public byte[] takeScreenshot(Region region, int thresholdType, int thresh, int maxval, float scale)
```

**参数：**
- `region` - 截图区域
- `thresholdType` - 算法类型
- `thresh` - 阈值
- `maxval` - 最大值
- `scale` - 缩放比例

**返回值：** 截图字节数组

### getWindowSize
获取屏幕大小

**方法签名：**
```java
public String getWindowSize()
```

**返回值：** "width|height" 格式

### getRotationAngle
获取屏幕旋转角度

**方法签名：**
```java
public int getRotationAngle()
```

**返回值：** 0, 90, 180, 270

## 点击和滑动 API

### click
点击

**方法签名：**
```java
public boolean click(int x, int y)
```

**参数：**
- `x` - X 坐标
- `y` - Y 坐标

**返回值：** 操作成功返回 true

### swipe
滑动

**方法签名：**
```java
public boolean swipe(int startX, int startY, int endX, int endY, int duration)
```

**参数：**
- `startX` - 起始 X 坐标
- `startY` - 起始 Y 坐标
- `endX` - 结束 X 坐标
- `endY` - 结束 Y 坐标
- `duration` - 持续时间（毫秒）

**返回值：** 操作成功返回 true

### longClick
长按

**方法签名：**
```java
public boolean longClick(int x, int y, int duration)
```

**参数：**
- `x` - X 坐标
- `y` - Y 坐标
- `duration` - 持续时间（毫秒）

**返回值：** 操作成功返回 true

### dispatchGestures
多点手势

**方法签名：**
```java
public boolean dispatchGestures(String gesturesPath)
```

**参数：**
- `gesturesPath` - 手势路径文件

**返回值：** 操作成功返回 true

## 文本输入 API

### typeText
输入文本

**方法签名：**
```java
public boolean typeText(String text)
```

**参数：**
- `text` - 要输入的文本

**返回值：** 操作成功返回 true

### clearText
清空文本

**方法签名：**
```java
public boolean clearText(int length)
```

**参数：**
- `length` - 要删除的字符数

**返回值：** 操作成功返回 true

## 图像识别 API

### findImage
查找图片

**方法签名：**
```java
public Point findImage(String imagePath, float similarity)
```

**参数：**
- `imagePath` - 图片路径
- `similarity` - 相似度（0.0-1.0）

**返回值：** 找到的图片位置，未找到返回 null

### findImages
查找多张图片

**方法签名：**
```java
public List<Point> findImages(String imagePath, float similarity, int maxCount)
```

**参数：**
- `imagePath` - 图片路径
- `similarity` - 相似度（0.0-1.0）
- `maxCount` - 最大查找数量

**返回值：** 找到的图片位置列表

## OCR 识别 API

### ocr
识别文字

**方法签名：**
```java
public List<OCRResult> ocr(Region region, int thresholdType, int thresh, int maxval, float scale)
```

**参数：**
- `region` - 识别区域
- `thresholdType` - 算法类型
- `thresh` - 阈值
- `maxval` - 最大值
- `scale` - 缩放比例

**返回值：** OCRResult 对象列表

### ocrText
获取屏幕文字

**方法签名：**
```java
public String ocrText(Region region)
```

**参数：**
- `region` - 识别区域

**返回值：** 识别出的文字

## UI 元素操作 API

### getElements
获取所有元素

**方法签名：**
```java
public JSONObject getElements()
```

**返回值：** 包含所有元素信息的 JSON 对象

### getElementRect
获取元素位置

**方法签名：**
```java
public Region getElementRect(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 元素位置区域

### getElementText
获取元素文本

**方法签名：**
```java
public String getElementText(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 元素文本内容

### setElementText
设置元素文本

**方法签名：**
```java
public boolean setElementText(String xpath, String text)
```

**参数：**
- `xpath` - 元素 XPath
- `text` - 要设置的文本

**返回值：** 操作成功返回 true

### clickElement
点击元素

**方法签名：**
```java
public boolean clickElement(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 操作成功返回 true

### existsElement
检查元素是否存在

**方法签名：**
```java
public boolean existsElement(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 元素存在返回 true

### isSelectedElement
检查元素是否选中

**方法签名：**
```java
public boolean isSelectedElement(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 元素被选中返回 true

### isEnabledElement
检查元素是否启用

**方法签名：**
```java
public boolean isEnabledElement(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 元素被启用返回 true

## HID 硬件控制 API

### hidPress
HID 按下

**方法签名：**
```java
public boolean hidPress(int x, int y)
```

**参数：**
- `x` - X 坐标
- `y` - Y 坐标

**返回值：** 操作成功返回 true

### hidMove
HID 移动

**方法签名：**
```java
public boolean hidMove(int x, int y, int duration)
```

**参数：**
- `x` - X 坐标
- `y` - Y 坐标
- `duration` - 持续时间

**返回值：** 操作成功返回 true

### hidClick
HID 点击

**方法签名：**
```java
public boolean hidClick(int x, int y)
```

**参数：**
- `x` - X 坐标
- `y` - Y 坐标

**返回值：** 操作成功返回 true

### hidSwipe
HID 滑动

**方法签名：**
```java
public boolean hidSwipe(int startX, int startY, int endX, int endY, int duration)
```

**参数：**
- `startX` - 起始 X 坐标
- `startY` - 起始 Y 坐标
- `endX` - 结束 X 坐标
- `endY` - 结束 Y 坐标
- `duration` - 持续时间

**返回值：** 操作成功返回 true

### hidDispatchGesture
HID 手势

**方法签名：**
```java
public boolean hidDispatchGesture(String gesturePath, int duration)
```

**参数：**
- `gesturePath` - 手势路径
- `duration` - 持续时间

**返回值：** 操作成功返回 true

### hidBack
HID 返回键

**方法签名：**
```java
public boolean hidBack()
```

**返回值：** 操作成功返回 true

### hidHome
HID Home 键

**方法签名：**
```java
public boolean hidHome()
```

**返回值：** 操作成功返回 true

## 验证码识别 API

### getCaptcha
识别验证码

**方法签名：**
```java
public JSONObject getCaptcha(String filePath, String username, String password, 
    String softId, String codeType, int lenMin)
```

**参数：**
- `filePath` - 图片文件路径
- `username` - 用户名
- `password` - 密码
- `softId` - 软件 ID
- `codeType` - 验证码类型
- `lenMin` - 最小长度

**返回值：** 包含识别结果的 JSON 对象

### errorCaptcha
报错返分

**方法签名：**
```java
public JSONObject errorCaptcha(String username, String password, String softId, String picId)
```

**参数：**
- `username` - 用户名
- `password` - 密码
- `softId` - 软件 ID
- `picId` - 图片 ID

**返回值：** 操作结果的 JSON 对象

### scoreCaptcha
查询剩余题分

**方法签名：**
```java
public JSONObject scoreCaptcha(String username, String password)
```

**参数：**
- `username` - 用户名
- `password` - 密码

**返回值：** 余额信息的 JSON 对象