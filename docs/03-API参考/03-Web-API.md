# Web API 参考

本章详细介绍 Web 自动化相关的 API。

## WebBot 类

[WebBot](file:///F:/workspace/github/aibote4j/sdk-core/src/main/java/net/aibote/sdk/WebBot.java) 是 Web 平台自动化的主要类。

## 浏览器操作 API

### startChrome
启动 Chrome

**方法签名：**
```java
public boolean startChrome(String chromeDriver, String profilePath)
```

**参数：**
- `chromeDriver` - ChromeDriver 路径
- `profilePath` - 配置文件路径

**返回值：** 启动成功返回 true

### navigate
页面导航

**方法签名：**
```java
public boolean navigate(String url)
```

**参数：**
- `url` - 要导航的 URL

**返回值：** 导航成功返回 true

### getCurrentUrl
获取当前 URL

**方法签名：**
```java
public String getCurrentUrl()
```

**返回值：** 当前页面 URL

### goBack
返回

**方法签名：**
```java
public boolean goBack()
```

**返回值：** 操作成功返回 true

### goForward
前进

**方法签名：**
```java
public boolean goForward()
```

**返回值：** 操作成功返回 true

### refresh
刷新

**方法签名：**
```java
public boolean refresh()
```

**返回值：** 操作成功返回 true

## 页面交互 API

### click
点击元素

**方法签名：**
```java
public boolean click(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 操作成功返回 true

### setValue
设置值

**方法签名：**
```java
public boolean setValue(String xpath, String value)
```

**参数：**
- `xpath` - 元素 XPath
- `value` - 要设置的值

**返回值：** 操作成功返回 true

### getText
获取文本

**方法签名：**
```java
public String getText(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 元素文本内容

### executeScript
执行 JavaScript

**方法签名：**
```java
public Object executeScript(String script, Object... args)
```

**参数：**
- `script` - 要执行的 JavaScript 代码
- `args` - 参数列表

**返回值：** 执行结果

## 标签页管理 API

### getCurPageId
获取当前页面 ID

**方法签名：**
```java
public String getCurPageId()
```

**返回值：** 当前页面 ID

### getAllPageId
获取所有页面 ID

**方法签名：**
```java
public String[] getAllPageId()
```

**返回值：** 页面 ID 数组

### switchPage
切换指定页面

**方法签名：**
```java
public boolean switchPage(String pageId)
```

**参数：**
- `pageId` - 要切换的页面 ID

**返回值：** 操作成功返回 true

### closePage
关闭当前页面

**方法签名：**
```java
public boolean closePage()
```

**返回值：** 操作成功返回 true

### newPage
新建标签页并跳转到指定 URL

**方法签名：**
```java
public boolean newPage(String url)
```

**参数：**
- `url` - 要跳转的 URL

**返回值：** 操作成功返回 true

## 页面信息 API

### getTitle
获取当前页面标题

**方法签名：**
```java
public String getTitle()
```

**返回值：** 页面标题

## 框架操作 API

### switchFrame
切换 frame

**方法签名：**
```java
public boolean switchFrame(String xpath)
```

**参数：**
- `xpath` - frame 元素路径

**返回值：** 操作成功返回 true

### switchMainFrame
切换到主 frame

**方法签名：**
```java
public boolean switchMainFrame()
```

**返回值：** 操作成功返回 true

## 元素操作 API

### getElementValue
获取编辑框值

**方法签名：**
```java
public String getElementValue(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 编辑框值，失败返回 null

### getElementText
获取元素文本

**方法签名：**
```java
public String getElementText(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 元素文本，失败返回 null

### getElementContent
获取元素内容

**方法签名：**
```java
public String getElementContent(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 元素内容，失败返回 null

### getElementOuterHTML
获取 outerHTML

**方法签名：**
```java
public String getElementOuterHTML(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** outerHTML，失败返回 null

### getElementInnerHTML
获取 innerHTML

**方法签名：**
```java
public String getElementInnerHTML(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** innerHTML，失败返回 null

### setElementAttribute
设置属性值

**方法签名：**
```java
public boolean setElementAttribute(String xpath, String name, String value)
```

**参数：**
- `xpath` - 元素 XPath
- `name` - 属性名
- `value` - 属性值

**返回值：** 操作成功返回 true

### getElementAttribute
获取指定属性的值

**方法签名：**
```java
public String getElementAttribute(String xpath, String name)
```

**参数：**
- `xpath` - 元素 XPath
- `name` - 属性名

**返回值：** 属性值，失败返回 null

### getElementRect
获取矩形位置

**方法签名：**
```java
public Region getElementRect(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 矩形位置对象，失败返回 null

### isSelected
判断元素是否选中

**方法签名：**
```java
public boolean isSelected(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 选中返回 true，否则返回 false

### isDisplayed
判断元素是否可见

**方法签名：**
```java
public boolean isDisplayed(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 可见返回 true，否则返回 false

### isEnabled
判断元素是否可用

**方法签名：**
```java
public boolean isEnabled(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 可用返回 true，否则返回 false

### clearElement
清空元素值

**方法签名：**
```java
public boolean clearElement(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 操作成功返回 true

### setElementFocus
设置元素焦点

**方法签名：**
```java
public boolean setElementFocus(String xpath)
```

**参数：**
- `xpath` - 元素 XPath

**返回值：** 操作成功返回 true

### uploadFile
通过元素上传文件

**方法签名：**
```java
public boolean uploadFile(String xpath, String filePath)
```

**参数：**
- `xpath` - 元素 XPath，上传文件路径一般含有 `<input type="file" >` 标签
- `filePath` - 本地文件路径

**返回值：** 操作成功返回 true

### showXpath
显示元素 xpath 路径

**方法签名：**
```java
public boolean showXpath()
```

**返回值：** 操作成功返回 true

**说明：** 调用此函数后，可在页面移动鼠标会显示元素区域。移动并按下 ctrl 键，会显示相对/绝对/文本 xpath 路径

### getElements
获取可见区域内的所有元素信息

**方法签名：**
```java
public JSONObject getElements()
```

**返回值：** 包含元素信息的 JSON 对象，失败返回 null

## 键盘操作 API

### sendKeys
输入文本

**方法签名：**
```java
public boolean sendKeys(String xpath, String text)
```

**参数：**
- `xpath` - 元素 XPath
- `text` - 要输入的文本

**返回值：** 操作成功返回 true

### sendVk
发送 Vk 虚拟键

**方法签名：**
```java
public boolean sendVk(int vkCode)
```

**参数：**
- `vkCode` - VK 键值

**返回值：** 操作成功返回 true

**支持的 VK 键值：**
- 8 - 退格键
- 9 - 制表键
- 13 - 回车键
- 32 - 空格键
- 37 - 方向左键
- 38 - 方向上键
- 39 - 方向右键
- 40 - 方向下键
- 46 - 删除键

## 鼠标操作 API

### clickMouse
点击鼠标

**方法签名：**
```java
public boolean clickMouse(int x, int y, int msg)
```

**参数：**
- `x` - 横坐标，非 Windows 坐标，页面左上角为起始坐标
- `y` - 纵坐标，非 Windows 坐标，页面左上角为起始坐标
- `msg` - 单击左键:1  单击右键:2  按下左键:3  弹起左键:4  按下右键:5  弹起右键:6  双击左键：7

**返回值：** 操作成功返回 true

### moveMouse
移动鼠标

**方法签名：**
```java
public boolean moveMouse(int x, int y)
```

**参数：**
- `x` - 横坐标
- `y` - 纵坐标

**返回值：** 操作成功返回 true

### wheelMouse
滚动鼠标

**方法签名：**
```java
public boolean wheelMouse(int deltaX, int deltaY, int x, int y)
```

**参数：**
- `deltaX` - 水平滚动条移动的距离，正数向右滚动，负数向左滚动
- `deltaY` - 垂直滚动条移动的距离，正数向下滚动，负数向上滚动
- `x` - 鼠标横坐标位置，默认在页面窗口中间
- `y` - 鼠标纵坐标位置，默认在页面窗口中间

**返回值：** 操作成功返回 true

### clickMouseByXpath
通过 xpath 点击鼠标

**方法签名：**
```java
public boolean clickMouseByXpath(String xpath, int msg)
```

**参数：**
- `xpath` - 元素路径
- `msg` - 单击左键:1  单击右键:2  按下左键:3  弹起左键:4  按下右键:5  弹起右键:6  双击左键：7

**返回值：** 操作成功返回 true

### moveMouseByXpath
通过 xpath 移动鼠标到元素的中心点

**方法签名：**
```java
public boolean moveMouseByXpath(String xpath)
```

**参数：**
- `xpath` - 元素路径

**返回值：** 操作成功返回 true

### wheelMouseByXpath
通过 xpath 滚动鼠标

**方法签名：**
```java
public boolean wheelMouseByXpath(String xpath, int deltaX, int deltaY)
```

**参数：**
- `xpath` - 元素路径
- `deltaX` - 水平滚动条移动的距离
- `deltaY` - 垂直滚动条移动的距离

**返回值：** 操作成功返回 true

## 触摸操作 API

### touchStart
仿真模式 开始触屏

**方法签名：**
```java
public boolean touchStart(int x, int y)
```

**参数：**
- `x` - 非 Windows 坐标，页面左上角为起始坐标
- `y` - 非 Windows 坐标，页面左上角为起始坐标

**返回值：** 操作成功返回 true

### touchMove
仿真模式 移动触屏

**方法签名：**
```java
public boolean touchMove(int x, int y)
```

**参数：**
- `x` - 非 Windows 坐标
- `y` - 非 Windows 坐标

**返回值：** 操作成功返回 true

### touchEnd
仿真模式 结束触屏

**方法签名：**
```java
public boolean touchEnd(int x, int y)
```

**参数：**
- `x` - 一般同最后一个触屏事件的坐标一致
- `y` - 一般同最后一个触屏事件的坐标一致

**返回值：** 操作成功返回 true

## 截图 API

### takeScreenshot
截图

**方法签名：**
```java
public String takeScreenshot(String xpath)
```

**参数：**
- `xpath` - 可选参数，元素路径。如果指定该参数则截取元素图片

**返回值：** PNG 图片格式 base64 字符串，失败返回 null

## Cookie 管理 API

### addCookie
添加 Cookie

**方法签名：**
```java
public boolean addCookie(String name, String value)
```

**参数：**
- `name` - Cookie 名称
- `value` - Cookie 值

**返回值：** 操作成功返回 true

### deleteCookie
删除 Cookie

**方法签名：**
```java
public boolean deleteCookie(String name)
```

**参数：**
- `name` - Cookie 名称

**返回值：** 操作成功返回 true

### getAllCookies
获取所有 Cookie

**方法签名：**
```java
public String getAllCookies()
```

**返回值：** 所有 Cookie 信息

## 隐式等待 API

### setImplicitTimeout
设置隐式等待

**方法签名：**
```java
public void setImplicitTimeout(int waitMs, int intervalMs)
```

**参数：**
- `waitMs` - 等待时间，单位毫秒
- `intervalMs` - 心跳间隔，单位毫秒