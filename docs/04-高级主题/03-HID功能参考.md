# HID 功能参考

本文档详细介绍了 Aibote4J 框架的 HID（Human Interface Device）功能。

## 什么是 HID

HID（Human Interface Device，人机接口设备）是一种标准的计算机接口，用于连接键盘、鼠标、游戏控制器等人机交互设备。在自动化领域，HID 功能允许我们直接控制设备的输入输出，绕过应用程序的限制。

## HID 在 Aibote4J 中的应用

在 Aibote4J 中，HID 功能主要用于 Android 设备的硬件级控制，提供更精准和可靠的输入操作。

## AndroidBot HID API

### hidPress
HID 按下操作

**方法签名：**
```java
public boolean hidPress(int x, int y)
```

**参数：**
- `x` - X 坐标
- `y` - Y 坐标

**返回值：** 操作成功返回 true

**示例：**
```java
// 在坐标 (500, 1000) 处按下
androidBot.hidPress(500, 1000);
```

### hidMove
HID 移动操作

**方法签名：**
```java
public boolean hidMove(int x, int y, int duration)
```

**参数：**
- `x` - X 坐标
- `y` - Y 坐标
- `duration` - 移动持续时间（毫秒）

**返回值：** 操作成功返回 true

**示例：**
```java
// 移动到坐标 (550, 1050)，持续 500 毫秒
androidBot.hidMove(550, 1050, 500);
```

### hidClick
HID 点击操作

**方法签名：**
```java
public boolean hidClick(int x, int y)
```

**参数：**
- `x` - X 坐标
- `y` - Y 坐标

**返回值：** 操作成功返回 true

**示例：**
```java
// 在坐标 (600, 800) 处点击
androidBot.hidClick(600, 800);
```

### hidSwipe
HID 滑动操作

**方法签名：**
```java
public boolean hidSwipe(int startX, int startY, int endX, int endY, int duration)
```

**参数：**
- `startX` - 起始 X 坐标
- `startY` - 起始 Y 坐标
- `endX` - 结束 X 坐标
- `endY` - 结束 Y 坐标
- `duration` - 滑动持续时间（毫秒）

**返回值：** 操作成功返回 true

**示例：**
```java
// 从 (500, 1500) 滑动到 (500, 500)，持续 1000 毫秒
androidBot.hidSwipe(500, 1500, 500, 500, 1000);
```

### hidDispatchGesture
HID 手势操作

**方法签名：**
```java
public boolean hidDispatchGesture(String gesturePath, int duration)
```

**参数：**
- `gesturePath` - 手势路径文件路径
- `duration` - 手势执行持续时间（毫秒）

**返回值：** 操作成功返回 true

**示例：**
```java
// 执行预定义的手势，持续 2000 毫秒
androidBot.hidDispatchGesture("swipe_up.json", 2000);
```

### hidBack
HID 返回键操作

**方法签名：**
```java
public boolean hidBack()
```

**返回值：** 操作成功返回 true

**示例：**
```java
// 按下返回键
androidBot.hidBack();
```

### hidHome
HID Home 键操作

**方法签名：**
```java
public boolean hidHome()
```

**返回值：** 操作成功返回 true

**示例：**
```java
// 按下 Home 键
androidBot.hidHome();
```

## HID 与其他输入方法的区别

### HID vs Touch
- **HID**: 硬件级操作，绕过应用安全机制，更稳定可靠
- **Touch**: 应用级操作，可能受到应用安全机制限制

### 适用场景
- **HID**: 适用于需要高度可靠性的场景，如自动化测试、安全应用操作
- **Touch**: 适用于常规自动化操作

## HID 使用示例

### 完整的 HID 自动化脚本

```java
public class HidAutomationExample extends AndroidBot {
    @Override
    public String getScriptName() {
        return "HidAutomationExample";
    }

    @Override
    public void doScript() {
        try {
            // 按下 Home 键回到主屏幕
            this.hidHome();
            this.sleep(1000);

            // 点击特定坐标启动应用
            this.hidClick(300, 500);
            this.sleep(2000);

            // 滑动屏幕
            this.hidSwipe(500, 1500, 500, 500, 1000);
            this.sleep(1000);

            // 执行复杂手势
            this.hidDispatchGesture("unlock_pattern.json", 2000);
            this.sleep(1000);

            // 按返回键
            this.hidBack();
            
            log.info("HID 自动化脚本执行完成");
        } catch (Exception e) {
            log.error("HID 自动化脚本执行失败", e);
        }
    }
}
```

### HID 与图像识别结合使用

```java
public void hidWithImageRecognition() {
    try {
        // 查找目标元素
        Point target = this.findImage("button.png", 0.9f);
        
        if (target != null) {
            // 使用 HID 精确点击找到的位置
            this.hidClick(target.x, target.y);
            log.info("通过 HID 点击了目标位置 ({}, {})", target.x, target.y);
        } else {
            log.warn("未找到目标图像");
        }
    } catch (Exception e) {
        log.error("HID 与图像识别结合操作失败", e);
    }
}
```

## 注意事项

### 安全权限
- 使用 HID 功能可能需要特殊的系统权限
- 某些 Android 设备可能限制 HID 操作
- 确保遵守应用和系统的使用条款

### 性能考虑
- HID 操作通常比普通触摸操作更消耗资源
- 合理安排操作间隔，避免过度频繁的操作
- 考虑设备性能差异，调整操作参数

### 兼容性
- 不同 Android 版本对 HID 的支持可能有所不同
- 不同设备制造商可能对 HID 功能有定制限制
- 建议在目标设备上充分测试

## 最佳实践

### 1. 操作验证
```java
// 执行 HID 操作后验证结果
boolean success = androidBot.hidClick(500, 1000);
if (success) {
    log.info("HID 点击操作成功");
    // 可以进一步验证操作结果，如截图对比
} else {
    log.error("HID 点击操作失败");
    // 尝试替代方案
}
```

### 2. 异常处理
```java
try {
    // HID 操作
    androidBot.hidSwipe(500, 1500, 500, 500, 1000);
} catch (Exception e) {
    log.error("HID 滑动操作失败，尝试替代方法", e);
    // 使用其他方法作为备选
    androidBot.swipe(500, 1500, 500, 500, 1000); // 使用普通滑动
}
```

### 3. 参数配置
```java
// 根据设备特性调整 HID 操作参数
int screenWidth = Integer.parseInt(this.getWindowSize().split("\\|")[0]);
int screenHeight = Integer.parseInt(this.getWindowSize().split("\\|")[1]);

// 基于屏幕尺寸调整操作参数
int centerX = screenWidth / 2;
int centerY = screenHeight / 2;
androidBot.hidClick(centerX, centerY);
```