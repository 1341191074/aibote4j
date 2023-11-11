package net.aibote.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import net.aibote.utils.dto.OCRResult;
import net.aibote.utils.options.Mode;
import net.aibote.utils.dto.Point;
import net.aibote.utils.options.Region;
import net.aibote.utils.options.SubColor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
public abstract class WinBot extends AiBot {
    private String serverIp = "127.0.0.1"; //默认本机
    private int serverPort = 0;
    private Stream<OCRResult> ocrResultStream;

    public static void startServer(Class<? extends WinBot> webBotClass, String serverIp, int serverPort, String driverPath) {
        WinBot winBot = null;
        try {
            winBot = webBotClass.newInstance();
            winBot.setServerIp(serverIp);
            winBot.setServerPort(serverPort);


        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (winBot.getServerPort() <= 0 || winBot.getServerPort() > 65535) {
            throw new RuntimeException("服务端口必须介于 0 ~ 65535");
        }

        if ("127.0.0.1".equals(winBot.getServerIp()) || "localhost".equals(winBot.getServerIp())) { //本机启动。
            try {
                String command = "WindowsDriver.exe";
                if (null != driverPath) {
                    command = driverPath + command;
                }
                command += " " + serverIp + " " + serverPort;
                //log.info(command);
                Process process = Runtime.getRuntime().exec(command);
                //log.info("启动driver");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // 创建 Socket 服务端，并设置监听的端口
        try (ServerSocket serverSocket = new ServerSocket(winBot.getServerPort())) {
            while (true) {
                // 阻塞方法，监听客户端请求
                Socket socket = serverSocket.accept();

                winBot.setClientCocket(socket);
                // 处理客户端请求
                //poolExecutor.execute(webBot);
                new Thread(winBot).start();
            }
        } catch (Exception ignored) {

        } finally {
            log.info("服务器关闭");
        }

    }

    /**
     * 查找窗口句柄
     *
     * @param className  窗口类名
     * @param windowName 窗口名
     * @return String 成功返回窗口句柄，失败返回null
     */
    public String findWindow(String className, String windowName) {
        return strCmd("findWindow", className, windowName);
    }

    /**
     * 查找窗口句柄数组，  以 “|” 分割
     *
     * @param className  窗口类名
     * @param windowName 窗口名
     * @return String 成功返回窗口句柄，失败返回null
     */
    public String findWindows(String className, String windowName) {
        return strCmd("findWindows", className, windowName);
    }

    /**
     * 查找窗口句柄
     *
     * @param curHwnd    当前窗口句柄
     * @param className  窗口类名
     * @param windowName 窗口名
     * @return String 成功返回窗口句柄，失败返回null
     */
    public String findSubWindow(String curHwnd, String className, String windowName) {
        return strCmd("findSubWindow", curHwnd, className, windowName);
    }

    /**
     * 查找父窗口句柄
     *
     * @param curHwnd 当前窗口句柄
     * @return String 成功返回窗口句柄，失败返回null
     */
    public String findParentWindow(String curHwnd) {
        return strCmd("findParentWindow", curHwnd);
    }

    /**
     * 查找桌面窗口句柄
     *
     * @return 成功返回窗口句柄，失败返回null
     */
    public String findDesktopWindow() {
        return strCmd("findDesktopWindow");
    }

    /**
     * 获取窗口名称
     *
     * @param hwnd 当前窗口句柄
     * @return String 成功返回窗口句柄，失败返回null
     */
    public String getWindowName(String hwnd) {
        return strCmd("getWindowName", hwnd);
    }

    /**
     * 显示/隐藏窗口
     *
     * @param hwnd   当前窗口句柄
     * @param isShow 是否显示
     * @return boolean  成功返回true，失败返回false
     */
    public boolean showWindow(String hwnd, boolean isShow) {
        return booleanCmd("showWindow", hwnd, String.valueOf(isShow));
    }

    /**
     * 显示/隐藏窗口
     *
     * @param hwnd  当前窗口句柄
     * @param isTop 是否置顶
     * @return boolean  成功返回true，失败返回false
     */
    public boolean setWindowTop(String hwnd, boolean isTop) {
        return booleanCmd("setWindowTop", hwnd, String.valueOf(isTop));
    }

    /**
     * 获取窗口位置。 用“|”分割
     *
     * @param hwnd 当前窗口句柄
     * @return 0|0|0|0
     */
    public String getWindowPos(String hwnd) {
        return strCmd("getWindowPos", hwnd);
    }

    /**
     * 设置窗口位置
     *
     * @param hwnd   当前窗口句柄
     * @param left   左上角横坐标
     * @param top    左上角纵坐标
     * @param width  width 窗口宽度
     * @param height height 窗口高度
     * @return boolean 成功返回true 失败返回 false
     */
    public boolean setWindowPos(String hwnd, int left, int top, int width, int height) {
        return booleanCmd("setWindowPos", hwnd, Integer.toString(left), Integer.toString(top), Integer.toString(width), Integer.toString(height));
    }

    /**
     * 移动鼠标 <br />
     * 如果mode值为true且目标控件有单独的句柄，则需要通过getElementWindow获得元素句柄，指定elementHwnd的值(极少应用窗口由父窗口响应消息，则无需指定)
     *
     * @param hwnd        窗口句柄
     * @param x           横坐标
     * @param y           纵坐标
     * @param mode        操作模式，后台 true，前台 false。默认前台操作。
     * @param elementHwnd 元素句柄
     * @return boolean 总是返回true
     */
    public boolean moveMouse(String hwnd, int x, int y, Mode mode, String elementHwnd) {
        return booleanCmd("moveMouse", hwnd, Integer.toString(x), Integer.toString(y), mode.boolValueStr(), elementHwnd);
    }

    /**
     * 移动鼠标(相对坐标)
     *
     * @param hwnd 窗口句柄
     * @param x    相对横坐标
     * @param y    相对纵坐标
     * @param mode 操作模式，后台 true，前台 false。默认前台操作
     * @return boolean 总是返回true
     */
    public boolean moveMouseRelative(String hwnd, int x, int y, Mode mode) {
        return booleanCmd("moveMouseRelative", hwnd, Integer.toString(x), Integer.toString(y), mode.boolValueStr());
    }

    /**
     * 滚动鼠标
     *
     * @param hwnd   窗口句柄
     * @param x      横坐标
     * @param y      纵坐标
     * @param dwData 鼠标滚动次数,负数下滚鼠标,正数上滚鼠标
     * @param mode   操作模式，后台 true，前台 false。默认前台操作
     * @return boolean 总是返回true
     */
    public boolean rollMouse(String hwnd, int x, int y, int dwData, Mode mode) {
        return booleanCmd("rollMouse", hwnd, Integer.toString(x), Integer.toString(y), Integer.toString(dwData), mode.boolValueStr());
    }

    /**
     * 鼠标点击<br />
     * 如果mode值为true且目标控件有单独的句柄，则需要通过getElementWindow获得元素句柄，指定elementHwnd的值(极少应用窗口由父窗口响应消息，则无需指定)
     *
     * @param hwnd        窗口句柄
     * @param x           横坐标
     * @param y           纵坐标
     * @param mouseType   单击左键:1 单击右键:2 按下左键:3 弹起左键:4 按下右键:5 弹起右键:6 双击左键:7 双击右键:8
     * @param mode        操作模式，后台 true，前台 false。默认前台操作。
     * @param elementHwnd 元素句柄
     * @return boolean 总是返回true。
     */
    public boolean clickMouse(String hwnd, int x, int y, int mouseType, int mode, String elementHwnd) {
        return booleanCmd("clickMouse", hwnd, Integer.toString(x), Integer.toString(y), Integer.toString(mouseType), Integer.toString(mode), elementHwnd);
    }

    /**
     * 输入文本
     *
     * @param txt 输入的文本
     * @return boolean 总是返回true
     */
    public boolean sendKeys(String txt) {
        return booleanCmd("sendKeys", txt);
    }

    /**
     * 后台输入文本
     *
     * @param hwnd 窗口句柄，如果目标控件有单独的句柄，需要通过getElementWindow获得句柄
     * @param txt  输入的文本
     * @return boolean 总是返回true
     */
    public boolean sendKeysByHwnd(String hwnd, String txt) {
        return booleanCmd("sendKeysByHwnd", hwnd, txt);
    }

    /**
     * 输入虚拟键值(VK)
     *
     * @param vk       VK键值，例如：回车对应 VK键值 13
     * @param keyState 按下弹起:1 按下:2 弹起:3
     * @return boolean 总是返回true
     */
    public boolean sendVk(int vk, int keyState) {
        return booleanCmd("sendVk", Integer.toString(vk), Integer.toString(keyState));
    }

    /**
     * 后台输入虚拟键值(VK)
     *
     * @param hwnd     窗口句柄，如果目标控件有单独的句柄，需要通过getElementWindow获得句柄
     * @param vk       VK键值，例如：回车对应 VK键值 13
     * @param keyState 按下弹起:1 按下:2 弹起:3
     * @return boolean 总是返回true
     */
    public boolean sendVkByHwnd(String hwnd, int vk, int keyState) {
        return booleanCmd("sendVkByHwnd", hwnd, Integer.toString(vk), Integer.toString(keyState));
    }

    /**
     * 截图保存。threshold默认保存原图。
     *
     * @param hwnd          窗口句柄
     * @param savePath      保存的位置
     * @param region        区域
     * @param thresholdType hresholdType算法类型。<br />
     *                      0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     *                      1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     *                      2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     *                      3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     *                      4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     *                      5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     *                      6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     * @param thresh        阈值。 thresh和maxval同为255时灰度处理
     * @param maxval        最大值。 thresh和maxval同为255时灰度处理
     * @return boolean
     */
    public boolean saveScreenshot(String hwnd, String savePath, Region region, int thresholdType, int thresh, int maxval) {
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }
        return booleanCmd("saveScreenshot", hwnd, savePath, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval));
    }

    /**
     * 获取指定坐标点的色值
     *
     * @param hwnd 窗口句柄
     * @param x    横坐标
     * @param y    纵坐标
     * @param mode 操作模式，后台 true，前台 false。默认前台操作
     * @return 成功返回#开头的颜色值，失败返回null
     */
    public String getColor(String hwnd, int x, int y, boolean mode) {
        return strCmd("getColor", hwnd, Integer.toString(x), Integer.toString(y), Boolean.toString(mode));
    }

    /**
     * @param hwndOrBigImagePath 窗口句柄或者图片路径
     * @param smallImagePath     小图片路径，多张小图查找应当用"|"分开小图路径
     * @param region             区域
     * @param sim                图片相似度 0.0-1.0，sim默认0.95
     * @param thresholdType      thresholdType算法类型：<br />
     *                           0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     *                           1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     *                           2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     *                           3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     *                           4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     *                           5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     *                           6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     * @param thresh             阈值。threshold默认保存原图。thresh和maxval同为255时灰度处理
     * @param maxval             最大值。threshold默认保存原图。thresh和maxval同为255时灰度处理
     * @param multi              找图数量，默认为1 找单个图片坐标
     * @param mode               操作模式，后台 true，前台 false。默认前台操作。hwndOrBigImagePath为图片文件，此参数无效
     * @return 成功返回 单坐标点[{x:number, y:number}]，多坐标点[{x1:number, y1:number}, {x2:number, y2:number}...] 失败返回null
     */
    public String findImages(String hwndOrBigImagePath, String smallImagePath, Region region, float sim, int thresholdType, int thresh, int maxval, int multi, Mode mode) {
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }

        String strData = null;
        if (hwndOrBigImagePath.toString().indexOf(".") == -1) {//在窗口上找图
            return strDelayCmd("findImage", hwndOrBigImagePath, smallImagePath, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Float.toString(sim), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval), Integer.toString(multi), mode.boolValueStr());
        } else {//在文件上找图
            return this.strDelayCmd("findImageByFile", hwndOrBigImagePath, smallImagePath, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Float.toString(sim), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval), Integer.toString(multi), mode.boolValueStr());
        }
    }

    /**
     * 找动态图
     *
     * @param hwnd      窗口句柄
     * @param frameRate 前后两张图相隔的时间，单位毫秒
     * @param mode      操作模式，后台 true，前台 false。默认前台操作
     * @return 成功返回 单坐标点[{x:number, y:number}]，多坐标点[{x1:number, y1:number}, {x2:number, y2:number}...] 失败返回null
     */
    public String findAnimation(String hwnd, int frameRate, Region region, Mode mode) {
        return strDelayCmd("findAnimation", hwnd, Integer.toString(frameRate), Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), mode.boolValueStr());
    }

    /**
     * 查找指定色值的坐标点
     *
     * @param hwnd         窗口句柄
     * @param strMainColor 颜色字符串，必须以 # 开头，例如：#008577；
     * @param subColors    辅助定位的其他颜色；
     * @param region       在指定区域内找色，默认全屏；
     * @param sim          相似度。0.0-1.0，sim默认为1
     * @param mode         后台 true，前台 false。默认前台操作。
     * @return String 成功返回 x|y 失败返回null
     */
    public String findColor(String hwnd, String strMainColor, SubColor[] subColors, Region region, float sim, Mode mode) {
        StringBuilder subColorsStr = new StringBuilder();
        if (null != subColors) {
            SubColor subColor;
            for (int i = 0; i < subColors.length; i++) {
                subColor = subColors[i];
                subColorsStr.append(subColor.offsetX).append("\n");
                subColorsStr.append(subColor.offsetY).append("\n");
                subColorsStr.append(subColor.colorStr);
                if (i < subColors.length - 1) { //最后不需要\n
                    subColorsStr.append("\n");
                }
            }
        }

        return this.strDelayCmd("findColor", hwnd, strMainColor, subColorsStr.toString(), Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Float.toString(sim), mode.boolValueStr());
    }

    /**
     * 比较指定坐标点的颜色值
     *
     * @param hwnd         窗口句柄
     * @param mainX        主颜色所在的X坐标
     * @param mainY        主颜色所在的Y坐标
     * @param mainColorStr 颜色字符串，必须以 # 开头，例如：#008577；
     * @param subColors    辅助定位的其他颜色；
     * @param region       截图区域 默认全屏
     * @param sim          相似度，0-1 的浮点数
     * @param mode         操作模式，后台 true，前台 false,
     * @return boolean
     */
    public boolean compareColor(String hwnd, int mainX, int mainY, String mainColorStr, SubColor[] subColors, Region region, float sim, Mode mode) {
        StringBuilder subColorsStr = new StringBuilder();
        if (null != subColors) {
            SubColor subColor;
            for (int i = 0; i < subColors.length; i++) {
                subColor = subColors[i];
                subColorsStr.append(subColor.offsetX).append("\n");
                subColorsStr.append(subColor.offsetY).append("\n");
                subColorsStr.append(subColor.colorStr);
                if (i < subColors.length - 1) { //最后不需要\n
                    subColorsStr.append("\n");
                }
            }
        }
        return this.booleanDelayCmd("compareColor", hwnd, Integer.toString(mainX), Integer.toString(mainY), mainColorStr, subColorsStr.toString(), Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Float.toString(sim), mode.boolValueStr());
    }

    /**
     * 提取视频帧
     *
     * @param videoPath  视频路径
     * @param saveFolder 提取的图片保存的文件夹目录
     * @param jumpFrame  跳帧，默认为1 不跳帧
     * @return boolean 成功返回true，失败返回false
     */
    public boolean extractImageByVideo(String videoPath, String saveFolder, int jumpFrame) {
        return this.booleanCmd("extractImageByVideo", videoPath, saveFolder, Integer.toString(jumpFrame));
    }

    /**
     * 裁剪图片
     *
     * @param imagePath  图片路径
     * @param saveFolder 裁剪后保存的图片路径
     * @param region     区域
     * @return boolean 成功返回true，失败返回false
     */
    public boolean cropImage(String imagePath, String saveFolder, Region region) {
        return this.booleanCmd("cropImage", imagePath, saveFolder, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom));
    }

    /**
     * 初始化ocr服务
     *
     * @param ocrServerIp   ocr服务器IP
     * @param ocrServerPort ocr服务器端口，默认9528。 注意，如果传入的值<=0 ，则都会当默认端口处理。
     * @return boolean 总是返回true
     */
    public boolean initOcr(String ocrServerIp, int ocrServerPort) {
        if (ocrServerPort <= 0) {
            ocrServerPort = 9528;
        }
        return this.booleanCmd("initOcr", ocrServerIp, Integer.toString(ocrServerPort));
    }

    /**
     * ocr识别
     *
     * @param hwnd          窗口句柄
     * @param region        区域
     * @param thresholdType 二值化算法类型
     * @param thresh        阈值
     * @param maxval        最大值
     * @param mode          操作模式，后台 true，前台 false。默认前台操作
     * @return String jsonstr
     */
    public List<OCRResult> ocr(String hwnd, Region region, int thresholdType, int thresh, int maxval, Mode mode) {
        if (null == region) {
            region = new Region();
        }
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }
        String strRet = this.strCmd("ocr", hwnd, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval), mode.boolValueStr());
        if (null == strRet || strRet == "" || strRet == "null" || strRet == "[]") {
            return null;
        } else {
            List<OCRResult> list = new ArrayList<>();
            JSONArray jsonArray = JSONArray.parseArray(strRet);
            jsonArray.forEach((ary) -> {
                if (ary instanceof JSONArray) {
                    JSONArray a = (JSONArray) ary;
                    OCRResult ocrResult = new OCRResult();
                    ocrResult.lt = new Point(a.getJSONArray(0).getJSONArray(0).getIntValue(0), a.getJSONArray(0).getJSONArray(0).getIntValue(1));
                    ocrResult.rt = new Point(a.getJSONArray(0).getJSONArray(1).getIntValue(0), a.getJSONArray(0).getJSONArray(1).getIntValue(1));
                    ocrResult.ld = new Point(a.getJSONArray(0).getJSONArray(2).getIntValue(0), a.getJSONArray(0).getJSONArray(2).getIntValue(1));
                    ocrResult.rd = new Point(a.getJSONArray(0).getJSONArray(3).getIntValue(0), a.getJSONArray(0).getJSONArray(3).getIntValue(1));
                    ocrResult.word = a.getJSONArray(1).getString(0);
                    ocrResult.rate = a.getJSONArray(1).getFloatValue(1);

                    list.add(ocrResult);
                }
            });
            return list;
        }
    }

    /**
     * ocr识别
     *
     * @param imagePath     图片路径
     * @param region        区域
     * @param thresholdType 二值化算法类型
     * @param thresh        阈值
     * @param maxval        最大值
     * @return String jsonstr
     */
    public List<OCRResult> ocrByFile(String imagePath, Region region, int thresholdType, int thresh, int maxval) {
        if (null == region) {
            region = new Region();
        }
        String strRet = this.strCmd("ocrByFile", imagePath, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval));
        if (strRet == null || strRet == "" || strRet == "null" || strRet == "[]") {
            return null;
        } else {
            List<OCRResult> list = new ArrayList<>();
            JSONArray jsonArray = JSONArray.parseArray(strRet);
            jsonArray.forEach((ary) -> {
                if (ary instanceof JSONArray) {
                    JSONArray a = (JSONArray) ary;
                    OCRResult ocrResult = new OCRResult();
                    ocrResult.lt = new Point(a.getJSONArray(0).getJSONArray(0).getIntValue(0), a.getJSONArray(0).getJSONArray(0).getIntValue(1));
                    ocrResult.rt = new Point(a.getJSONArray(0).getJSONArray(1).getIntValue(0), a.getJSONArray(0).getJSONArray(1).getIntValue(1));
                    ocrResult.ld = new Point(a.getJSONArray(0).getJSONArray(2).getIntValue(0), a.getJSONArray(0).getJSONArray(2).getIntValue(1));
                    ocrResult.rd = new Point(a.getJSONArray(0).getJSONArray(3).getIntValue(0), a.getJSONArray(0).getJSONArray(3).getIntValue(1));
                    ocrResult.word = a.getJSONArray(1).getString(0);
                    ocrResult.rate = a.getJSONArray(1).getFloatValue(1);

                    list.add(ocrResult);
                }
            });
            return list;
        }
    }

    /**
     * 获取屏幕文字
     *
     * @param hwndOrImagePath 窗口句柄或者图片路径
     * @param region          区域
     * @param thresholdType   thresholdType算法类型：<br />
     *                        0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0<br />
     *                        1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva<br />
     *                        2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0<br />
     *                        3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变<br />
     *                        4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变<br />
     *                        5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值<br />
     *                        6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     * @param thresh          阈值
     * @param maxval          最大值
     * @param mode            后台 true，前台 false。默认前台操作, 仅适用于hwnd
     * @return 失败返回null，成功返窗口上的文字
     */
    public String getWords(String hwndOrImagePath, Region region, int thresholdType, int thresh, int maxval, Mode mode) {
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }

        List<OCRResult> wordsResult = null;
        if (hwndOrImagePath.indexOf(".") == -1) {
            wordsResult = this.ocr(hwndOrImagePath, region, thresholdType, thresh, maxval, mode);
        } else {
            wordsResult = this.ocrByFile(hwndOrImagePath, region, thresholdType, thresh, maxval);
        }

        if (null == wordsResult) {
            return null;
        }

        StringBuilder words = new StringBuilder();
        wordsResult.forEach((obj) -> {
            words.append(obj.word).append("\n");
        });

        return words.toString();
    }

    /**
     * 查找文字
     *
     * @param hwndOrImagePath 窗口句柄或者图片路径
     * @param word            要查找的文字
     * @param region          区域
     * @param thresholdType   算法类型：<br />
     *                        *                        0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0<br />
     *                        *                        1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva<br />
     *                        *                        2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0<br />
     *                        *                        3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变<br />
     *                        *                        4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变<br />
     *                        *                        5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值<br />
     *                        *                        6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     * @param thresh          阈值
     * @param maxval          最大值
     * @param mode            后台 true，前台 false。默认前台操作, 仅适用于hwnd
     * @return Point
     */
    public Point findWords(String hwndOrImagePath, String word, Region region, int thresholdType, int thresh, int maxval, Mode mode) {
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }

        List<OCRResult> wordsResult = null;
        if (hwndOrImagePath.indexOf(".") == -1) {
            wordsResult = this.ocr(hwndOrImagePath, region, thresholdType, thresh, maxval, mode);
        } else {
            wordsResult = this.ocrByFile(hwndOrImagePath, region, thresholdType, thresh, maxval);
        }

        if (null == wordsResult) {
            return null;
        }

        Point point = new Point(-1, -1);
        StringBuilder words = new StringBuilder();
        Optional<OCRResult> first = wordsResult.stream().filter((y) -> y.word.indexOf(word) != -1).findFirst();
        if (first.isPresent()) {
            OCRResult ocrResult = first.get();
            int localLeft, localTop, localRight, localBottom, width, height, wordWidth, offsetX, offsetY, index, x, y;
            localLeft = ocrResult.lt.x;
            localTop = ocrResult.lt.y;
            localRight = ocrResult.ld.x;
            localBottom = ocrResult.ld.y;
            width = localRight - localLeft;
            height = localBottom - localTop;
            wordWidth = width / ocrResult.word.length();
            index = ocrResult.word.indexOf(word);
            offsetX = wordWidth * (index + words.length() / 2);
            offsetY = height / 2;
            x = (localLeft + offsetX + region.left);
            y = (localTop + offsetY + region.top);
            point.x = x;
            point.y = y;
        }
        return point;
    }

    /**
     * 获取指定元素名称
     *
     * @param hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
     * @param xpath 元素路径 getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @return 成功返回元素名称
     */
    public String getElementName(String hwnd, String xpath) {
        return this.strDelayCmd("getElementName", hwnd, xpath);
    }

    /**
     * 获取指定元素文本
     *
     * @param hwnd  窗口句柄
     * @param xpath 元素路径
     * @return 成功返回元素文本
     */
    public String getElementValue(String hwnd, String xpath) {
        return this.strDelayCmd("getElementValue", hwnd, xpath);
    }

    /**
     * 获取指定元素矩形大小
     *
     * @param hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
     *                   * getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param xpath 元素路径
     * @return Region
     */
    public Region getElementRect(String hwnd, String xpath) {
        String retStr = this.strDelayCmd("getElementRect", hwnd, xpath);

        if (null == retStr || retStr == "-1|-1|-1|-1") {
            return null;
        }

        String[] arrRet = retStr.split("|");
        Region region = new Region();
        region.left = Integer.valueOf(arrRet[0]);
        region.top = Integer.valueOf(arrRet[1]);
        region.right = Integer.valueOf(arrRet[2]);
        region.bottom = Integer.valueOf(arrRet[3]);
        return region;
    }

    /**
     * 获取元素窗口句柄
     *
     * @param hwnd  窗口句柄
     * @param xpath 元素路径
     * @return 成功返回元素窗口句柄，失败返回null
     */
    public String getElementWindow(String hwnd, String xpath) {
        return this.strDelayCmd("getElementWindow", hwnd, xpath);
    }

    /**
     * 点击元素
     *
     * @param hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
     *              getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param xpath 元素路径
     * @param opt   单击左键:1 单击右键:2 按下左键:3 弹起左键:4 按下右键:5 弹起右键:6 双击左键:7 双击右键:8
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    public boolean clickElement(String hwnd, String xpath, String opt) {
        return this.booleanDelayCmd("clickElement", hwnd, xpath, opt);
    }

    /**
     * 执行元素默认操作(一般是点击操作)
     *
     * @param {string|number} hwnd  窗口句柄。
     * @param {string}        xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    public boolean invokeElement(String hwnd, String xpath) {
        return this.booleanDelayCmd("invokeElement", hwnd, xpath);
    }

    /**
     * 设置指定元素作为焦点
     *
     * @param {string|number} hwnd  窗口句柄
     * @param {string}        xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    public boolean setElementFocus(String hwnd, String xpath) {
        return this.booleanDelayCmd("setElementFocus", hwnd, xpath);
    }

    /**
     * 设置元素文本
     *
     * @param hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
     *              getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param xpath 元素路径
     * @param value 要设置的内容
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    public boolean setElementValue(String hwnd, String xpath, String value) {
        return this.booleanDelayCmd("setElementValue", hwnd, xpath, value);
    }

    /**
     * 滚动元素
     *
     * @param hwnd              窗口句柄
     * @param xpath             元素路径
     * @param horizontalPercent 水平百分比 -1不滚动
     * @param verticalPercent   垂直百分比 -1不滚动
     * @return 成功返回true 失败返回 false
     */
    public boolean setElementScroll(String hwnd, String xpath, float horizontalPercent, float verticalPercent) {
        return this.booleanDelayCmd("setElementScroll", hwnd, xpath, Float.toString(horizontalPercent), Float.toString(verticalPercent));
    }

    /**
     * 单/复选框是否选中
     *
     * @param hwnd  窗口句柄
     * @param xpath 元素路径
     * @return 成功返回true 失败返回 false
     */
    public boolean isSelected(String hwnd, String xpath) {
        String strRet = this.strDelayCmd("isSelected", hwnd, xpath);
        if ("selected".equals(strRet)) return true;
        else return false;
    }

    /**
     * 关闭窗口
     *
     * @param hwnd  窗口句柄
     * @param xpath 元素路径
     * @return 成功返回true 失败返回 false
     */
    public boolean closeWindow(String hwnd, String xpath) {
        return booleanCmd("closeWindow", hwnd, xpath);
    }

    /**
     * 设置窗口状态
     *
     * @param hwnd  hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
     *              getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param xpath 元素路径
     * @param state 0正常 1最大化 2 最小化
     * @return boolean
     */
    public boolean setWindowState(String hwnd, String xpath, int state) {
        return booleanCmd("setWindowState", hwnd, xpath, Integer.toString(state));
    }

    /**
     * 设置剪贴板
     *
     * @param text 文字内容
     * @return boolean
     */
    public boolean setClipboardText(String text) {
        return booleanCmd("setClipboardText", text);
    }

    /**
     * 获取剪贴板内容
     *
     * @return
     */
    public String getClipboardText() {
        return strCmd("getClipboardText");
    }

    /**
     * 启动指定程序
     *
     * @param commandLine 启动命令行
     * @param showWindow  是否显示窗口。可选参数,默认显示窗口
     * @param isWait      是否等待程序结束。可选参数,默认不等待
     * @return {Promise.<boolean>} 成功返回true,失败返回false
     */
    public boolean startProcess(String commandLine, boolean showWindow, boolean isWait) {
        return booleanCmd("startProcess", commandLine, Boolean.toString(showWindow), Boolean.toString(isWait));
    }

    /**
     * 执行cmd命令
     *
     * @param command     cmd命令，不能含 "cmd"字串
     * @param waitTimeout 可选参数，等待结果返回超时，单位毫秒，默认300毫秒
     * @return {Promise.<string>} 返回cmd执行结果
     */
    public String executeCommand(String command, int waitTimeout) {
        return strCmd("executeCommand", command, Integer.toString(waitTimeout));
    }

    /**
     * 指定url下载文件
     *
     * @param url      文件地址
     * @param filePath 文件保存的路径
     * @param isWait   是否等待.为true时,等待下载完成
     * @return {Promise.<boolean>} 总是返回true
     */
    public boolean downloadFile(String url, String filePath, boolean isWait) {
        return booleanCmd("downloadFile", url, filePath, Boolean.toString(isWait));
    }

    /**
     * 打开excel文档
     *
     * @param excelPath excle路径
     * @return {Promise.<Object>} 成功返回excel对象，失败返回null
     */
    public JSONObject openExcel(String excelPath) {
        String strRet = strCmd("openExcel", excelPath);
        return JSON.parseObject(strRet);
    }

    /**
     * 打开excel表格
     *
     * @param excelObject excel对象
     * @param sheetName   表名
     * @return {Promise.<Object>} 成功返回sheet对象，失败返回null
     */
    public JSONObject openExcelSheet(JSONObject excelObject, String sheetName) {
        String strRet = strCmd("openExcelSheet", excelObject.getString("book"), excelObject.getString("path"), sheetName);
        return JSON.parseObject(strRet);
    }

    /**
     * 保存excel文档
     *
     * @param excelObject excel对象
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean saveExcel(JSONObject excelObject) {
        return booleanCmd("saveExcel", excelObject.getString("book"), excelObject.getString("path"));
    }

    /**
     * 写入数字到excel表格
     *
     * @param sheetObject sheet对象
     * @param row         行
     * @param col         列
     * @param value       写入的值
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean writeExcelNum(JSONObject sheetObject, int row, int col, int value) {
        return booleanCmd("writeExcelNum", sheetObject.toJSONString(), Integer.toString(row), Integer.toString(col), Integer.toString(value));
    }

    /**
     * 写入字符串到excel表格
     *
     * @param sheetObject sheet对象
     * @param row         行
     * @param col         列
     * @param strValue    写入的值
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean writeExcelStr(JSONObject sheetObject, int row, int col, String strValue) {
        return booleanCmd("writeExcelStr", sheetObject.toJSONString(), Integer.toString(row), Integer.toString(col), strValue);
    }

    /**
     * 读取excel表格数字
     *
     * @param sheetObject sheet对象
     * @param row         行
     * @param col         列
     * @return {Promise.<number>} 返回读取到的数字
     */
    public Float readExcelNum(JSONObject sheetObject, int row, int col) {
        String strRet = strCmd("readExcelNum", sheetObject.toJSONString(), Integer.toString(row), Integer.toString(col));
        return Float.valueOf(strRet);
    }

    /**
     * 读取excel表格数字
     *
     * @param sheetObject sheet对象
     * @param row         行
     * @param col         列
     * @return {Promise.<number>} 返回读取到的数字
     */
    public String readExcelStr(JSONObject sheetObject, int row, int col) {
        return strCmd("readExcelStr", sheetObject.toJSONString(), Integer.toString(row), Integer.toString(col));
    }

    /**
     * 删除excel表格行
     *
     * @param sheetObject sheet对象
     * @param rowFirst    起始行
     * @param rowLast     结束行
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean removeExcelRow(JSONObject sheetObject, int rowFirst, int rowLast) {
        return booleanCmd("removeExcelRow", sheetObject.toJSONString(), Integer.toString(rowFirst), Integer.toString(rowLast));
    }

}
