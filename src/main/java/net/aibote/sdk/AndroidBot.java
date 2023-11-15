package net.aibote.sdk;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.aibote.sdk.dto.OCRResult;
import net.aibote.sdk.dto.Point;
import net.aibote.sdk.options.GesturePath;
import net.aibote.sdk.options.Mode;
import net.aibote.sdk.options.Region;
import net.aibote.sdk.options.SubColor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AndroidBot extends AiBot {
    private String serverIp = "127.0.0.1"; //默认本机
    private int serverPort = 0;
    private Stream<OCRResult> ocrResultStream;

    public static void startServer(Class<? extends AndroidBot> webBotClass, String serverIp, int serverPort, String driverPath) {
        AndroidBot androidBot = null;
        try {
            androidBot = webBotClass.newInstance();
            androidBot.setServerIp(serverIp);
            androidBot.setServerPort(serverPort);

        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (androidBot.getServerPort() <= 0 || androidBot.getServerPort() > 65535) {
            throw new RuntimeException("服务端口必须介于 0 ~ 65535");
        }

        // 创建 Socket 服务端，并设置监听的端口
        try (ServerSocket serverSocket = new ServerSocket(androidBot.getServerPort())) {
            while (true) {
                // 阻塞方法，监听客户端请求
                Socket socket = serverSocket.accept();

                androidBot.setClientCocket(socket);
                // 处理客户端请求
                //poolExecutor.execute(webBot);
                ChannelMap.getChannelMap().put(String.valueOf(socket.getInetAddress()), socket);
                new Thread(androidBot).start();
            }
        } catch (Exception ignored) {

        } finally {
            log.info("服务器关闭");
        }

    }

    /**
     * 截图保存<br />
     * 截图保存在客户端本地了
     *
     * @param savePath      保存的位置
     * @param region        截图区域      [10, 20, 100, 200]，region默认全屏
     * @param thresholdType 算法类型：
     *                      0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     *                      1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     *                      2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     *                      3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     *                      4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     *                      5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     *                      6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     *                      thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * @return {Promise.<boolean>}
     */
    public boolean saveScreenshot(String savePath, Region region, int thresholdType, int thresh, int maxval) {
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }

        return this.booleanCmd("saveScreenshot", savePath, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval));
    }

    /**
     * 截图<br />
     * 截图数据以bytes方式返回服务端
     *
     * @param region        截图区域      [10, 20, 100, 200]，region默认全屏
     * @param thresholdType 算法类型：
     *                      0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     *                      1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     *                      2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     *                      3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     *                      4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     *                      5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     *                      6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     *                      thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * @param scale         保存的位置
     * @return {Promise.<boolean>}
     */
    public byte[] takeScreenshot(String savePath, Region region, int thresholdType, int thresh, int maxval, float scale) {
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }

        return this.bytesCmd("saveScreenshot", Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval), Float.toString(scale));
    }

    /**
     * 获取指定坐标点的色值
     *
     * @param x 横坐标
     * @param y 纵坐标
     * @return {Promise.<string>} 成功返回#开头的颜色值，失败返回null
     */
    public String getColor(int x, int y) {
        return this.strCmd("getColor", Integer.toString(x), Integer.toString(y));
    }

    /**
     * 找图
     *
     * @param imagePath     小图片路径，多张小图查找应当用"|"分开小图路径
     * @param region        区域
     * @param sim           图片相似度 0.0-1.0，sim默认0.95
     * @param thresholdType thresholdType算法类型：<br />
     *                      0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     *                      1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     *                      2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     *                      3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     *                      4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     *                      5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     *                      6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     * @param thresh        阈值。threshold默认保存原图。thresh和maxval同为255时灰度处理
     * @param maxval        最大值。threshold默认保存原图。thresh和maxval同为255时灰度处理
     * @param multi         找图数量，默认为1 找单个图片坐标
     * @param mode          操作模式，后台 true，前台 false。默认前台操作。hwndOrBigImagePath为图片文件，此参数无效
     * @return 成功返回 单坐标点[{x:number, y:number}]，多坐标点[{x1:number, y1:number}, {x2:number, y2:number}...] 失败返回null
     */
    public String findImages(String imagePath, Region region, float sim, int thresholdType, int thresh, int maxval, int multi, Mode mode) {
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }

        return this.strDelayCmd("findImage", imagePath, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Float.toString(sim), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval), Integer.toString(multi), mode.boolValueStr());
    }

    /**
     * 找动态图
     *
     * @param frameRate 前后两张图相隔的时间，单位毫秒
     * @param frameRate 前后两张图相隔的时间，单位毫秒
     * @return 成功返回 单坐标点[{x:number, y:number}]，多坐标点[{x1:number, y1:number}, {x2:number, y2:number}...] 失败返回null
     */
    public String findAnimation(int frameRate, Region region) {
        return strDelayCmd("findAnimation", Integer.toString(frameRate), Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom));
    }


    /**
     * 查找指定色值的坐标点
     *
     * @param strMainColor 颜色字符串，必须以 # 开头，例如：#008577；
     * @param subColors    辅助定位的其他颜色；
     * @param region       在指定区域内找色，默认全屏；
     * @param sim          相似度。0.0-1.0，sim默认为1
     * @param mode         后台 true，前台 false。默认前台操作。
     * @return String 成功返回 x|y 失败返回null
     */
    public String findColor(String strMainColor, SubColor[] subColors, Region region, float sim, Mode mode) {
        StringBuilder subColorsStr = new StringBuilder();
        if (null != subColors) {
            SubColor subColor;
            for (int i = 0; i < subColors.length; i++) {
                subColor = subColors[i];
                subColorsStr.append(subColor.offsetX).append("/");
                subColorsStr.append(subColor.offsetY).append("/");
                subColorsStr.append(subColor.colorStr);
                if (i < subColors.length - 1) { //最后不需要\n
                    subColorsStr.append("\n");
                }
            }
        }

        return this.strDelayCmd("findColor", strMainColor, subColorsStr.toString(), Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Float.toString(sim), mode.boolValueStr());
    }

    /**
     * 比较指定坐标点的颜色值
     *
     * @param mainX        主颜色所在的X坐标
     * @param mainY        主颜色所在的Y坐标
     * @param mainColorStr 颜色字符串，必须以 # 开头，例如：#008577；
     * @param subColors    辅助定位的其他颜色；
     * @param region       截图区域 默认全屏
     * @param sim          相似度，0-1 的浮点数
     * @param mode         操作模式，后台 true，前台 false,
     * @return boolean
     */
    public boolean compareColor(int mainX, int mainY, String mainColorStr, SubColor[] subColors, Region region, float sim, Mode mode) {
        StringBuilder subColorsStr = new StringBuilder();
        if (null != subColors) {
            SubColor subColor;
            for (int i = 0; i < subColors.length; i++) {
                subColor = subColors[i];
                subColorsStr.append(subColor.offsetX).append("/");
                subColorsStr.append(subColor.offsetY).append("/");
                subColorsStr.append(subColor.colorStr);
                if (i < subColors.length - 1) { //最后不需要\n
                    subColorsStr.append("\n");
                }
            }
        }
        return this.booleanDelayCmd("compareColor", Integer.toString(mainX), Integer.toString(mainY), mainColorStr, subColorsStr.toString(), Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Float.toString(sim), mode.boolValueStr());
    }

    /**
     * 手指按下
     *
     * @param x        横坐标
     * @param y        纵坐标
     * @param duration 按下时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean press(int x, int y, int duration) {
        return this.booleanCmd("press", Integer.toString(x), Integer.toString(y), Integer.toString(duration));
    }


    /**
     * 手指移动
     *
     * @param x        横坐标
     * @param y        纵坐标
     * @param duration 移动时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean move(int x, int y, int duration) {
        return this.booleanCmd("move", Integer.toString(x), Integer.toString(y), Integer.toString(duration));
    }

    /**
     * 手指释放
     *
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean release() {
        return this.booleanCmd("release");
    }


    /**
     * 点击坐标
     *
     * @param x 横坐标
     * @param y 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean click(int x, int y) {
        return this.booleanCmd("click", Integer.toString(x), Integer.toString(y));
    }

    /**
     * 双击坐标
     *
     * @param x 横坐标
     * @param y 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean doubleClick(int x, int y) {
        return this.booleanCmd("doubleClick", Integer.toString(x), Integer.toString(y));
    }

    /**
     * 长按坐标
     *
     * @param x        横坐标
     * @param y        纵坐标
     * @param duration 长按时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean longClick(int x, int y, int duration) {
        return this.booleanCmd("longClick", Integer.toString(x), Integer.toString(y), Integer.toString(duration));
    }

    /**
     * 滑动坐标
     *
     * @param {number} startX 起始横坐标
     * @param {number} startY 起始纵坐标
     * @param {number} endX 结束横坐标
     * @param {number} endY 结束纵坐标
     * @param {number} duration 滑动时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean swipe(int startX, int startY, int endX, int endY, float duration) {
        return this.booleanCmd("swipe", Integer.toString(startX), Integer.toString(startY), Integer.toString(endX), Integer.toString(endY), Float.toString(duration));
    }


    /**
     * 执行手势
     *
     * @param gesturePath 手势路径
     * @param duration    手势时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean dispatchGesture(GesturePath gesturePath, float duration) {
        return this.booleanCmd("dispatchGesture", gesturePath.gesturePathStr("\n"), Float.toString(duration));
    }


    /**
     * 发送文本
     *
     * @param text 发送的文本，需要打开aibote输入法
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean sendKeys(String text) {
        return this.booleanCmd("sendKeys", text);
    }

    /**
     * 发送按键
     *
     * @param {number} keyCode 发送的虚拟按键，需要打开aibote输入法。例如：最近应用列表：187  回车：66
     *                 按键对照表 https://blog.csdn.net/yaoyaozaiye/article/details/122826340
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean sendVk(int keyCode) {
        return this.booleanCmd("sendVk", Integer.toString(keyCode));
    }

    /**
     * 返回
     *
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean back() {
        return this.booleanCmd("back");
    }

    /**
     * home
     *
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean home() {
        return this.booleanCmd("home");
    }

    /**
     * 显示最近任务
     *
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean recents() {
        return this.booleanCmd("recents");
    }

    /**
     * 打开 开/关机 对话框，基于无障碍权限
     *
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean powerDialog() {
        return this.booleanCmd("powerDialog");
    }

    /**
     * 初始化ocr服务
     *
     * @param {string} ocrServerIp  ocr服务器IP。当参数值为 "127.0.0.1"时，则使用手机内置的ocr识别，不必打开AiboteAndroidOcr.exe服务端
     * @param {number} ocrServerPort ocr服务器端口，默认9527
     * @return {Promise.<boolean>} 总是返回true
     */
    public boolean initOcr(String ocrServerIp, int ocrServerPort) {
        if (ocrServerPort <= 0) {
            ocrServerPort = 9527;
        }
        return this.booleanCmd("initOcr", ocrServerIp, Integer.toString(ocrServerPort));
    }

    /**
     * ocr识别
     *
     * @param region        区域
     * @param thresholdType 二值化算法类型
     * @param thresh        阈值
     * @param maxval        最大值
     * @param mode          操作模式，后台 true，前台 false。默认前台操作
     * @return String jsonstr
     */
    public List<OCRResult> ocr(Region region, int thresholdType, int thresh, int maxval, Mode mode) {
        if (null == region) {
            region = new Region();
        }
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }
        String strRet = this.strCmd("ocr", Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval), mode.boolValueStr());
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
     * 获取屏幕文字
     *
     * @param region        区域
     * @param thresholdType thresholdType算法类型：<br />
     *                      0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0<br />
     *                      1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva<br />
     *                      2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0<br />
     *                      3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变<br />
     *                      4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变<br />
     *                      5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值<br />
     *                      6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     * @param thresh        阈值
     * @param maxval        最大值
     * @param mode          后台 true，前台 false。默认前台操作, 仅适用于hwnd
     * @return 失败返回null，成功返窗口上的文字
     */
    public String getWords(Region region, int thresholdType, int thresh, int maxval, Mode mode) {
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }

        List<OCRResult> wordsResult = null;
        wordsResult = this.ocr(region, thresholdType, thresh, maxval, mode);

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
     * @param word          要查找的文字
     * @param region        区域
     * @param thresholdType 算法类型：<br />
     *                      *                        0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0<br />
     *                      *                        1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva<br />
     *                      *                        2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0<br />
     *                      *                        3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变<br />
     *                      *                        4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变<br />
     *                      *                        5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值<br />
     *                      *                        6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     * @param thresh        阈值
     * @param maxval        最大值
     * @param mode          后台 true，前台 false。默认前台操作, 仅适用于hwnd
     * @return Point
     */
    public Point findWords(String word, Region region, int thresholdType, int thresh, int maxval, Mode mode) {
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }

        List<OCRResult> wordsResult = null;
        wordsResult = this.ocr(region, thresholdType, thresh, maxval, mode);


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
     * URL请求
     *
     * @param url         请求的地址 http://www.ai-bot.net
     * @param requestType 请求类型，GET或者POST
     * @param headers     可选参数，请求头
     * @param postData    可选参数，用作POST 提交的数据
     * @return {Promise.<string>} 返回请求数据内容
     */
    public String urlRequest(String url, String requestType, String headers, String postData) {
        return this.strCmd("urlRequest", url, requestType, headers, postData);
    }

    /**
     * Toast消息提示
     *
     * @param text     提示的文本
     * @param duration 显示时长，最大时长3500毫秒
     * @return {Promise.<boolean>} 返回true
     */
    public boolean showToast(String text, float duration) {
        return this.booleanCmd("showToast", text, Float.toString(duration));
    }

    /**
     * 启动App
     *
     * @param name 包名或者app名称
     * @return {Promise.<boolean>} 成功返回true 失败返回false。非Aibote界面时候调用，需要开启悬浮窗
     */
    public boolean startApp(String name) {
        return this.booleanCmd("startApp", name);
    }

    /**
     * 判断app是否正在运行(包含前后台)
     *
     * @param name 包名或者app名称
     * @return {Promise.<boolean>} 正在运行返回true，否则返回false
     */
    public boolean appIsRunnig(String name) {
        return this.booleanCmd("appIsRunnig", name);
    }

    /**
     * 获取已安装app的包名(不包含系统APP)
     *
     * @return 成功返回已安装app包名数组(使用 | 分割)，失败返回null
     */
    public String getInstalledPackages() {
        return this.strCmd("getInstalledPackages");
    }

    /**
     * 屏幕大小
     *
     * @return 成功返回屏幕大小使用 | 分割
     */
    public String getWindowSize() {
        return this.strCmd("getWindowSize");
    }

    /**
     * 图片大小
     *
     * @param imagePath 图片路径
     * @return 成功返回 图片大小使用 | 分割
     */
    public String getImageSize(String imagePath) {
        return this.strCmd("getImageSize", imagePath);
    }

    /**
     * 获取安卓ID
     *
     * @return {Promise.<string>} 成功返回安卓手机ID
     */
    public String getAndroidId() {
        return this.strCmd("getAndroidId");
    }

    /**
     * 获取投屏组号
     *
     * @return {Promise.<string>} 成功返回投屏组号
     */
    public String getGroup() {
        return this.strCmd("getGroup");
    }

    /**
     * 获取投屏编号
     *
     * @return {Promise.<string>} 成功返回投屏编号
     */
    public String getIdentifier() {
        return this.strCmd("getIdentifier");
    }

    /**
     * 获取投屏标题
     *
     * @return {Promise.<string>} 成功返回投屏标题
     */
    public String getTitle() {
        return this.strCmd("getTitle");
    }

    /**
     * 识别验证码
     *
     * @param filePath 图片文件路径
     * @param username 用户名
     * @param password 密码
     * @param softId   软件ID
     * @param codeType 图片类型 参考https://www.chaojiying.com/price.html
     * @param lenMin   最小位数 默认0为不启用,图片类型为可变位长时可启用这个参数
     * @return {Promise.<{err_no:number, err_str:string, pic_id:string, pic_str:string, md5:string}>} 返回JSON
     * err_no,(数值) 返回代码  为0 表示正常，错误代码 参考https://www.chaojiying.com/api-23.html
     * err_str,(字符串) 中文描述的返回信息
     * pic_id,(字符串) 图片标识号，或图片id号
     * pic_str,(字符串) 识别出的结果
     * md5,(字符串) md5校验值,用来校验此条数据返回是否真实有效
     */
    public JSONObject getCaptcha(String filePath, String username, String password, String softId, String codeType, int lenMin) {
        String strRet = this.strCmd("getCaptcha", filePath, username, password, softId, codeType, Integer.toString(lenMin));
        return JSONObject.parse(strRet);
    }

    /**
     * 识别报错返分
     *
     * @param username 用户名
     * @param password 密码
     * @param softId   软件ID
     * @param picId    图片ID 对应 getCaptcha返回值的pic_id 字段
     * @return {Promise.<{err_no:number, err_str:string}>} 返回JSON
     * err_no,(数值) 返回代码
     * err_str,(字符串) 中文描述的返回信息
     */
    public JSONObject errorCaptcha(String username, String password, String softId, String picId) {
        String strRet = this.strCmd("errorCaptcha", username, password, softId, picId);
        return JSONObject.parse(strRet);
    }


    /**
     * 查询验证码剩余题分
     *
     * @param username 用户名
     * @param password 密码
     * @return {Promise.<{err_no:number, err_str:string, tifen:string, tifen_lock:string}>} 返回JSON
     * err_no,(数值) 返回代码
     * err_str,(字符串) 中文描述的返回信息
     * tifen,(数值) 题分
     * tifen_lock,(数值) 锁定题分
     */
    public JSONObject scoreCaptcha(String username, String password) {
        String strRet = this.strCmd("scoreCaptcha", username, password);
        return JSONObject.parse(strRet);
    }

    /**
     * 获取元素位置
     *
     * @param xpath 元素路径
     * @return {Promise.<{left:number, top:number, right:number, bottom:number}>} 成功返回元素位置，失败返回null
     */
    public Region getElementRect(String xpath) {
        String strRet = this.strDelayCmd("getElementRect", xpath);
        String[] arrRet = strRet.split("\\|");
        Region region = new Region();
        region.left = Integer.parseInt(arrRet[0]);
        region.top = Integer.parseInt(arrRet[1]);
        region.right = Integer.parseInt(arrRet[2]);
        region.bottom = Integer.parseInt(arrRet[3]);
        return region;
    }

    /**
     * 获取元素描述
     *
     * @param xpath 元素路径
     * @return {Promise.<string>} 成功返回元素内容，失败返回null
     */
    public String getElementDescription(String xpath) {
        return this.strDelayCmd("getElementDescription", xpath);
    }

    /**
     * 获取元素文本
     *
     * @param xpath 元素路径
     * @return {Promise.<string>} 成功返回元素内容，失败返回null
     */
    public String strDelayCmd(String xpath) {
        return this.setSendData("getElementText", xpath);
    }

    /**
     * 判断元素是否可见
     *
     * @param xpath 元素路径
     * @return {Promise.<boolean>} 可见 ture，不可见 false
     */
    public boolean elementIsVisible(String xpath) {
        String windowRect = this.getWindowSize();
        Region elementRect = this.getElementRect(xpath);
        if (elementRect == null)
            return false;

        String[] split = windowRect.split("\\|");
        int elementWidth = elementRect.right - elementRect.left;
        int elementHeight = elementRect.bottom - elementRect.top;
        if (elementRect.top < 0 || elementRect.left < 0 || elementWidth > Integer.parseInt(split[0]) || elementHeight > Integer.parseInt(split[1]))
            return false;
        else
            return true;
    }

    /**
     * 设置元素文本
     *
     * @param xpath 元素路径
     * @param text  设置的文本
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean setElementText(String xpath, String text) {
        return this.booleanDelayCmd("setElementText", xpath, text);
    }

    /**
     * 点击元素
     *
     * @param xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean clickElement(String xpath) {
        return this.booleanDelayCmd("clickElement", xpath);
    }

    /**
     * 滚动元素
     *
     * @param xpath     元素路径
     * @param direction 0 向前滑动， 1 向后滑动
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean scrollElement(String xpath, int direction) {
        return this.booleanDelayCmd("scrollElement", xpath, Integer.toString(direction));
    }

    /**
     * 判断元素是否存在
     *
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean existsElement(String xpath) {
        return this.booleanCmd("existsElement", xpath);
    }

    /**
     * 判断元素是否选中
     *
     * @param xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean isSelectedElement(String xpath) {
        return this.booleanCmd("isSelectedElement", xpath);
    }

    /**
     * 上传文件
     *
     * @param {string} windowsFilePath 电脑文件路径，注意电脑路径 "\\"转义问题
     * @param {string} androidFilePath 安卓文件保存路径, 安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
//    async pushFile(windowsFilePath, androidFilePath) {
//        let fileData = await fs.readFileSync(windowsFilePath);
//        let byteData = await this.setSendFile("pushFile", androidFilePath, fileData);
//        let byteRet = await this.sendData(byteData);
//        let strRet = byteRet.toString();
//        if (strRet == "false")
//            return false;
//        else
//            return true;
//    }

    /**
     * 拉取文件
     *
     * @param {string} androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @param {string} windowsFilePath 电脑文件保存路径，注意电脑路径 "\\"转义问题
     * @return {Promise.<void>}
     */
    public void pullFile(String androidFilePath, String windowsFilePath) throws IOException {
        byte[] byteData = this.bytesCmd("pullFile", androidFilePath);
        FileUtils.writeByteArrayToFile(new File(windowsFilePath), byteData, true);
    }

    /**
     * 写入安卓文件
     *
     * @param {string}  androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @param {string}  text 写入的内容
     * @param {boolean} isAppend 可选参数，是否追加，默认覆盖文件内容
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    public boolean writeAndroidFile(String androidFilePath, String text, boolean isAppend) {
        return this.booleanCmd("writeAndroidFile", androidFilePath, text, Boolean.toString(isAppend));
    }

    /**
     * 读取安卓文件
     *
     * @param {string} androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<string>} 成功返回文件内容，失败返回 null
     */
    public String readAndroidFile(String androidFilePath) {
        return this.strCmd("readAndroidFile", androidFilePath);
    }

    /**
     * 删除安卓文件
     *
     * @param androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    public String deleteAndroidFile(String androidFilePath) {
        return this.strCmd("deleteAndroidFile", androidFilePath);
    }

    /**
     * 判断文件是否存在
     *
     * @param androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    public String existsAndroidFile(String androidFilePath) {
        return this.strCmd("existsAndroidFile", androidFilePath);
    }

    /**
     * 获取文件夹内的所有文件(不包含深层子目录)
     *
     * @param androidDirectory 安卓目录，安卓外部存储根目录 /storage/emulated/0/
     * @return 成功返回所有子文件名称，用|分割，失败返回null
     */
    public String getAndroidSubFiles(String androidDirectory) {
        return this.strCmd("getAndroidSubFiles", androidDirectory);
    }

    /**
     * 创建安卓文件夹
     *
     * @param {string} androidDirectory 安卓目录
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    public boolean makeAndroidDir(String androidDirectory) {
        return this.booleanCmd("makeAndroidDir", androidDirectory);
    }

}
