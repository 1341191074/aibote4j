- 2024-12-26 Reach
```
WindowsBot
       1、增加playAudio 播放音频文件函数
        2、数字人模型文件夹内包含其他无关文件不能初始化问题
        3、优化多线程安全问题
        4、修复部分电脑声音克隆服务生成lab文件失败问题
        5、增加PlayMedia 播放视频函数
        6、增加ExtractAudio导出音频函数
        7、修复metahumanInsertVideo 函数导致数字人播报混乱问题

WebBot   1、优化多线程安全问题

远程投屏   1、优化多线程安全问题
```
- 2024-05-13 Reach
```
webbot：
【新增】activateFrame 函数

windows：
【新增】getWindowsId 函数
```

- 2024-04-13 Reach
```
webbot
【新增】showXpath，显示元素xpath路径

android：
【新增】downloadFile，下载网络文件到手机

windows：
【新增】metahumanSpeechByFile，initSpeechCloneService，metahumanSpeechClone，makeMetahumanVideoClone
```

- 2023-11-26 Reach
```
【新增】框架微调， 新增AndroidBotServer、WebBotServer、WinBoteServer 三个类，可以在main直接使用  
【新增】框架支持读取yml配置文件，例： String myconf = (String) super.ymlConfig.get("myconf");
                                System.out.println(myconf);

android：
【调整】initOcr 新增参数，useAngleModel，enableGPU， enableTensorrt
【新增】initYolo yolo函数

windows：
【调整】initOcr 新增参数，useAngleModel，enableGPU， enableTensorrt
【新增】initYolo yoloByHwnd yoloByFile 函数
```

- 2023-11-19 Reach
``` 
【升级】jdk升级为21，当前版本不向下兼容。
【增加】程序支持发全局连接监控，可以使用ChannelMap获取挡墙的所有连接。
【优化】升级到jdk21后，线程启动使用虚拟线程。
【修复】部分命令发送后，返回数据包长度为0，导致的线程阻塞问题。
```

- 2023-11-18 Reach 此版本需将aibote升级到 2023-11-18 发布的版本。 不支持向下兼容
```
WinBot.initOcr 参数调整，以支持aibote内置OCR。 
WebBot新增3个仿真函数，touchStart,touchMove,touchEnd
AiBot类增加getVersion() 方法。 返回 2023-11-18 。 （ 目前以群内发布的日期为版本号，等待增加官方版本号 ）
```

- 2023-11-17 Reach
``` 
修复android传参错误的问题
修正WebBot中的方法名称
```

- 2023-11-16 Reach
``` 
AndroidBot 初版完成
```

- 2023-11-11 Reach
``` 
WinBot、WebBot  初版完成
```

- 2023-11-08 Reach
``` 
Initial commit
```
