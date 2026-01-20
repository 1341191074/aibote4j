package net.aibote.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.aibote.sdk.dto.OCRResult;
import net.aibote.sdk.dto.Point;
import net.aibote.sdk.options.Mode;
import net.aibote.sdk.options.Region;
import net.aibote.sdk.options.SubColor;
import net.aibote.utils.HttpClientUtils;
import net.aibote.utils.ImageBase64Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class WinBot extends AbstractPlatformBot {

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
        return boolCmd("showWindow", hwnd, String.valueOf(isShow));
    }

    /**
     * 显示/隐藏窗口
     *
     * @param hwnd  当前窗口句柄
     * @param isTop 是否置顶
     * @return boolean  成功返回true，失败返回false
     */
    public boolean setWindowTop(String hwnd, boolean isTop) {
        return boolCmd("setWindowTop", hwnd, String.valueOf(isTop));
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
        return boolCmd("setWindowPos", hwnd, Integer.toString(left), Integer.toString(top), Integer.toString(width), Integer.toString(height));
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
        return boolCmd("moveMouse", hwnd, Integer.toString(x), Integer.toString(y), mode.boolValueStr(), elementHwnd);
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
        return boolCmd("moveMouseRelative", hwnd, Integer.toString(x), Integer.toString(y), mode.boolValueStr());
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
        return boolCmd("rollMouse", hwnd, Integer.toString(x), Integer.toString(y), Integer.toString(dwData), mode.boolValueStr());
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
    public boolean clickMouse(String hwnd, int x, int y, int mouseType, Mode mode, String elementHwnd) {
        return boolCmd("clickMouse", hwnd, Integer.toString(x), Integer.toString(y), Integer.toString(mouseType), mode.boolValueStr(), elementHwnd);
    }

    /**
     * 输入文本
     *
     * @param txt 输入的文本
     * @return boolean 总是返回true
     */
    public boolean sendKeys(String txt) {
        return boolCmd("sendKeys", txt);
    }

    /**
     * 后台输入文本
     *
     * @param hwnd 窗口句柄，如果目标控件有单独的句柄，需要通过getElementWindow获得句柄
     * @param txt  输入的文本
     * @return boolean 总是返回true
     */
    public boolean sendKeysByHwnd(String hwnd, String txt) {
        return boolCmd("sendKeysByHwnd", hwnd, txt);
    }

    /**
     * 输入虚拟键值(VK)
     *
     * @param vk       VK键值，例如：回车对应 VK键值 13
     * @param keyState 按下弹起:1 按下:2 弹起:3
     * @return boolean 总是返回true
     */
    public boolean sendVk(int vk, int keyState) {
        return boolCmd("sendVk", Integer.toString(vk), Integer.toString(keyState));
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
        return boolCmd("sendVkByHwnd", hwnd, Integer.toString(vk), Integer.toString(keyState));
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
        return boolCmd("saveScreenshot", hwnd, savePath, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval));
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
                subColorsStr.append(subColor.offsetX).append("/");
                subColorsStr.append(subColor.offsetY).append("/");
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
                subColorsStr.append(subColor.offsetX).append("/");
                subColorsStr.append(subColor.offsetY).append("/");
                subColorsStr.append(subColor.colorStr);
                if (i < subColors.length - 1) { //最后不需要\n
                    subColorsStr.append("\n");
                }
            }
        }
        return this.boolDelayCmd("compareColor", hwnd, Integer.toString(mainX), Integer.toString(mainY), mainColorStr, subColorsStr.toString(), Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Float.toString(sim), mode.boolValueStr());
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
        return this.boolCmd("extractImageByVideo", videoPath, saveFolder, Integer.toString(jumpFrame));
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
        return this.boolCmd("cropImage", imagePath, saveFolder, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom));
    }

    /**
     * 初始化ocr服务
     *
     * @param ocrServerIp    ocr服务器IP
     * @param ocrServerPort  ocr服务器端口，固定端口9527。 注意，如果传入的值<=0 ，则都会当默认端口处理。
     * @param useAngleModel  支持图像旋转。 默认false。仅内置ocr有效。内置OCR需要安装
     * @param enableGPU      启动GPU 模式。默认false 。GPU模式需要电脑安装NVIDIA驱动，并且到群文件下载对应cuda版本
     * @param enableTensorrt 启动加速，仅 enableGPU = true 时有效，默认false 。图片太大可能会导致GPU内存不足
     * @return boolean 总是返回true
     */
    public boolean initOcr(String ocrServerIp, int ocrServerPort, boolean useAngleModel, boolean enableGPU, boolean enableTensorrt) {
        //if (ocrServerPort <= 0) {
        ocrServerPort = 9527;
        //}
        return this.boolCmd("initOcr", ocrServerIp, Integer.toString(ocrServerPort), Boolean.toString(useAngleModel), Boolean.toString(enableGPU), Boolean.toString(enableTensorrt));
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
    public List<OCRResult> ocrByHwnd(String hwnd, Region region, int thresholdType, int thresh, int maxval, Mode mode) {
        if (null == region) {
            region = new Region();
        }
        if (thresholdType == 5 || thresholdType == 6) {
            thresh = 127;
            maxval = 255;
        }
        String strRet = this.strCmd("ocrByHwnd", hwnd, Integer.toString(region.left), Integer.toString(region.top), Integer.toString(region.right), Integer.toString(region.bottom), Integer.toString(thresholdType), Integer.toString(thresh), Integer.toString(maxval), mode.boolValueStr());
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
            wordsResult = this.ocrByHwnd(hwndOrImagePath, region, thresholdType, thresh, maxval, mode);
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
            wordsResult = this.ocrByHwnd(hwndOrImagePath, region, thresholdType, thresh, maxval, mode);
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
     * 初始化yolo服务
     *
     * @param yoloServerIp yolo服务器IP。端口固定为9528
     * @param modelPath    模型路径
     * @param classesPath  种类路径，CPU模式需要此参数
     * @return {Promise.<boolean>} 总是返回true
     */
    public boolean initYolo(String yoloServerIp, String modelPath, String classesPath) {
        return this.boolCmd("initYolo", yoloServerIp, modelPath, classesPath);
    }

    /**
     * yoloByHwnd
     *
     * @param hwnd 窗口句柄
     * @param mode 操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<[]>} 失败返回null，成功返回数组形式的识别结果
     */
    public JSONArray yoloByHwnd(String hwnd, Mode mode) {
        String strRet = this.strCmd("yoloByHwnd", hwnd, mode.boolValueStr());
        if (StringUtils.isNotBlank(strRet)) {
            return JSONArray.parse(strRet);
        }
        return null;
    }

    /**
     * yoloByFile
     *
     * @param imagePath 图片路径
     * @return {Promise.<[]>} 失败返回null，成功返回数组形式的识别结果
     */
    public JSONArray yoloByFile(String imagePath) {
        String strRet = this.strCmd("yoloByFile", imagePath);
        if (StringUtils.isNotBlank(strRet)) {
            return JSONArray.parse(strRet);
        }
        return null;
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
        return this.boolDelayCmd("clickElement", hwnd, xpath, opt);
    }

    /**
     * 执行元素默认操作(一般是点击操作)
     *
     * @param {string|number} hwnd  窗口句柄。
     * @param {string}        xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    public boolean invokeElement(String hwnd, String xpath) {
        return this.boolDelayCmd("invokeElement", hwnd, xpath);
    }

    /**
     * 设置指定元素作为焦点
     *
     * @param {string|number} hwnd  窗口句柄
     * @param {string}        xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    public boolean setElementFocus(String hwnd, String xpath) {
        return this.boolDelayCmd("setElementFocus", hwnd, xpath);
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
        return this.boolDelayCmd("setElementValue", hwnd, xpath, value);
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
        return this.boolDelayCmd("setElementScroll", hwnd, xpath, Float.toString(horizontalPercent), Float.toString(verticalPercent));
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
        return boolCmd("closeWindow", hwnd, xpath);
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
        return boolCmd("setWindowState", hwnd, xpath, Integer.toString(state));
    }

    /**
     * 设置剪贴板
     *
     * @param text 文字内容
     * @return boolean
     */
    public boolean setClipboardText(String text) {
        return boolCmd("setClipboardText", text);
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
        return boolCmd("startProcess", commandLine, Boolean.toString(showWindow), Boolean.toString(isWait));
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
        return boolCmd("downloadFile", url, filePath, Boolean.toString(isWait));
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
        return boolCmd("saveExcel", excelObject.getString("book"), excelObject.getString("path"));
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
        return boolCmd("writeExcelNum", sheetObject.toJSONString(), Integer.toString(row), Integer.toString(col), Integer.toString(value));
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
        return boolCmd("writeExcelStr", sheetObject.toJSONString(), Integer.toString(row), Integer.toString(col), strValue);
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
        return boolCmd("removeExcelRow", sheetObject.toJSONString(), Integer.toString(rowFirst), Integer.toString(rowLast));
    }

    /**
     * 删除excel表格列
     *
     * @param sheetObject sheet对象
     * @param rowFirst    起始列
     * @param rowLast     结束列
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean removeExcelCol(JSONObject sheetObject, int rowFirst, int rowLast) {
        return boolCmd("removeExcelCol", sheetObject.toJSONString(), Integer.toString(rowFirst), Integer.toString(rowLast));
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
    public JSONObject getCaptcha(String filePath, String username, String password, String softId, String codeType, String lenMin) throws Exception {
        if (StringUtils.isBlank(lenMin)) {
            lenMin = "0";
        }

        String file_base64 = ImageBase64Converter.convertFileToBase64(filePath);

        String url = "http://upload.chaojiying.net/Upload/Processing.php";
        JSONObject dataJsonObject = new JSONObject();
        dataJsonObject.put("user", username);
        dataJsonObject.put("pass", password);
        dataJsonObject.put("softid", softId);
        dataJsonObject.put("codetype", codeType);
        dataJsonObject.put("len_min", lenMin);
        dataJsonObject.put("file_base64", file_base64);

        JSONObject paramJsonObject = new JSONObject();
        paramJsonObject.put("multipart", true);
        paramJsonObject.put("data", dataJsonObject);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:24.0) Gecko/20100101 Firefox/24.0");
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String retStr = HttpClientUtils.doPost(url, paramJsonObject.toJSONString(), headers);
        return JSONObject.parseObject(retStr);

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
    public JSONObject errorCaptcha(String username, String password, String softId, String picId) throws Exception {

        String url = "http://upload.chaojiying.net/Upload/ReportError.php";
        JSONObject dataJsonObject = new JSONObject();
        dataJsonObject.put("user", username);
        dataJsonObject.put("pass", password);
        dataJsonObject.put("softid", softId);
        dataJsonObject.put("id", picId);

        JSONObject paramJsonObject = new JSONObject();
        paramJsonObject.put("multipart", true);
        paramJsonObject.put("data", dataJsonObject);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:24.0) Gecko/20100101 Firefox/24.0");
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String retStr = HttpClientUtils.doPost(url, paramJsonObject.toJSONString(), headers);
        return JSONObject.parseObject(retStr);
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
    public JSONObject scoreCaptcha(String username, String password) throws Exception {

        String url = "http://upload.chaojiying.net/Upload/GetScore.php";
        JSONObject dataJsonObject = new JSONObject();
        dataJsonObject.put("user", username);
        dataJsonObject.put("pass", password);

        JSONObject paramJsonObject = new JSONObject();
        paramJsonObject.put("multipart", true);
        paramJsonObject.put("data", dataJsonObject);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:24.0) Gecko/20100101 Firefox/24.0");
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String retStr = HttpClientUtils.doPost(url, paramJsonObject.toJSONString(), headers);
        return JSONObject.parseObject(retStr);
    }

    /**
     * 初始化语音服务(不支持win7)
     *
     * @param speechKey,    微软语音API密钥
     * @param speechRegion, 区域
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    public boolean removeExcelCol(String speechKey, String speechRegion) {
        return boolCmd("initSpeechService", speechKey, speechRegion);
    }

    /**
     * 音频文件转文本
     *
     * @param filePath, 音频文件路径
     * @param language, 语言，参考开发文档 语言和发音人
     * @return {Promise.<string || null>} 成功返回转换后的音频文本，失败返回null
     */
    public String audioFileToText(String filePath, String language) {
        return strCmd("audioFileToText", filePath, language);
    }

    /**
     * 麦克风输入流转换文本
     *
     * @param language, 语言，参考开发文档 语言和发音人
     * @return {Promise.<string || null>} 成功返回转换后的音频文本，失败返回null
     */
    public String microphoneToText(String language) {
        return strCmd("microphoneToText", language);
    }

    /**
     * 文本合成音频到扬声器
     *
     * @param ssmlPathOrText，要转换语音的文本或者".xml"格式文件路径
     * @param language，语言，参考开发文档                    语言和发音人
     * @param voiceName，发音人，参考开发文档                  语言和发音人
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean textToBullhorn(String ssmlPathOrText, String language, String voiceName) {
        return boolCmd("textToBullhorn", ssmlPathOrText, language, voiceName);
    }

    /**
     * 文本合成音频并保存到文件
     *
     * @param ssmlPathOrText，要转换语音的文本或者".xml"格式文件路径
     * @param language，语言，参考开发文档                    语言和发音人
     * @param voiceName，发音人，参考开发文档                  语言和发音人
     * @param audioPath，保存音频文件路径
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean textToAudioFile(String ssmlPathOrText, String language, String voiceName, String audioPath) {
        return boolCmd("textToAudioFile", ssmlPathOrText, language, voiceName);
    }

    /**
     * 麦克风音频翻译成目标语言文本
     *
     * @param sourceLanguage，要翻译的语言，参考开发文档 语言和发音人
     * @param targetLanguage，翻译后的语言，参考开发文档 语言和发音人
     * @return {Promise.<string || null>} 成功返回翻译后的语言文本，失败返回null
     */
    public String microphoneTranslationText(String sourceLanguage, String targetLanguage) {
        return strCmd("microphoneTranslationText", sourceLanguage, targetLanguage);
    }

    /**
     * 音频文件翻译成目标语言文本
     *
     * @param audioPath，                   要翻译的音频文件路径
     * @param sourceLanguage，要翻译的语言，参考开发文档 语言和发音人
     * @param targetLanguage，翻译后的语言，参考开发文档 语言和发音人
     * @return {Promise.<string || null>}成功返回翻译后的语言文本，失败返回null
     */
    public String audioFileTranslationText(String audioPath, String sourceLanguage, String targetLanguage) {
        return strCmd("audioFileTranslationText", audioPath, sourceLanguage, targetLanguage);
    }

    /**
     * 初始化数字人，第一次初始化需要一些时间
     *
     * @param metahumanModePath,   数字人模型路径
     * @param metahumanScaleValue, 数字人缩放倍数，1为原始大小。为0.5时放大一倍，2则缩小一半
     * @param isUpdateMetahuman,   是否强制更新，默认fasle。为true时强制更新会拖慢初始化速度
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean initMetahuman(String metahumanModePath, float metahumanScaleValue, boolean isUpdateMetahuman, boolean enableRandomImage) {
        return boolCmd("initMetahuman", metahumanModePath, Float.toString(metahumanScaleValue), Boolean.toString(isUpdateMetahuman), Boolean.toString(enableRandomImage));
    }

    /**
     * 数字人说话，此函数需要调用 initSpeechService 初始化语音服务
     *
     * @param saveVoiceFolder, 保存的发音文件目录，文件名以0开始依次增加，扩展为.wav格式
     * @param text             要转换语音的文本
     * @param language         语言，参考开发文档                       语言和发音人
     * @param voiceName        发音人，参考开发文档                     语言和发音人
     * @param quality          音质，0低品质                          1中品质  2高品质， 默认为0低品质
     * @param waitPlaySound    等待音频播报完毕，true等待/false不等待
     * @param speechRate       语速，默认为0，取值范围 -100 至 200
     * @param voiceStyle       语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean metahumanSpeech(String saveVoiceFolder, String text, String language, String voiceName, int quality, boolean waitPlaySound, int speechRate, String voiceStyle) {
        if (StringUtils.isBlank(voiceStyle)) {
            voiceStyle = "General";
        }
        return this.boolCmd("metahumanSpeech", saveVoiceFolder, text, language, voiceName, Integer.toString(quality), Boolean.toString(waitPlaySound), Integer.toString(speechRate), voiceStyle);
    }

    /**
     * 数字人说话缓存模式，需要调用 initSpeechService 初始化语音服务。函数一般用于常用的话术播报，非常用话术切勿使用，否则内存泄漏
     *
     * @param saveVoiceFolder 保存的发音文件目录，文件名以0开始依次增加，扩展为.wav格式
     * @param text            要转换语音的文本
     * @param language        语言，参考开发文档                       语言和发音人
     * @param voiceName       发音人，参考开发文档                     语言和发音人
     * @param quality         音质，0低品质                          1中品质  2高品质， 默认为0低品质
     * @param waitPlaySound   等待音频播报完毕，true等待/false不等待
     * @param speechRate      语速，默认为0，取值范围 -100 至 200
     * @param voiceStyle      语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean metahumanSpeechCache(String saveVoiceFolder, String text, String language, String voiceName, int quality, boolean waitPlaySound, int speechRate, String voiceStyle) {
        if (StringUtils.isBlank(voiceStyle)) {
            voiceStyle = "General";
        }
        return this.boolCmd("metahumanSpeechCache", saveVoiceFolder, text, language, voiceName, Integer.toString(quality), Boolean.toString(waitPlaySound), Integer.toString(speechRate), voiceStyle);
    }


    /**
     * 数字人说话文件缓存模式
     *
     * @param {string}  audioPath, 音频路径， 同名的 .lab文件需要和音频文件在同一目录下
     * @param {boolean} waitPlaySound，等待音频播报完毕， true等待/false不等待
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean metahumanSpeechByFile(String audioPath, boolean waitPlaySound) {
        return boolCmd("metahumanSpeechByFile", audioPath, Boolean.toString(waitPlaySound));
    }


    /**
     * 数字人说话文件缓存模式(Ex) metahumanSpeechByFileEx 不能与 PlayAudioEx 同步执行
     *
     * @param {string}  audioPath, 音频路径， 同名的 .lab文件需要和音频文件在同一目录下。若.lab文件不存在，则自动生成.lab文件。生成.lab文件产生的费用，请联系管理员
     * @param {boolean} enableRandomParam, 是否启用随机去重参数
     * @param {boolean} waitPlaySound，等待音频播报完毕，默认为 false等待。为false时 多次调用此函数会添加到队列按顺序播报
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean metahumanSpeechByFileEx(String audioPath, boolean enableRandomParam, boolean waitPlaySound) {
        return boolCmd("metahumanSpeechByFileEx", audioPath, Boolean.toString(enableRandomParam), Boolean.toString(waitPlaySound));
    }

    /**
     * `
     * 打断数字人说话，一般用作人机对话场景。
     * metahumanSpeech和metahumanSpeechCache的 waitPlaySound 参数 设置为false时，此函数才有意义
     *
     * @return {Promise.<boolean>} 返回true打断正在说话， 返回false 则为未说话状态
     */
    public boolean metahumanSpeechBreak() {
        return boolCmd("metahumanSpeechBreak");
    }

    /**
     * 数字人插入视频
     *
     * @param videoFilePath  插入的视频文件路径
     * @param audioFilePath  插入的视频文件路径
     * @param audioFilePath, 插入的音频文件路径
     * @param waitPlayVideo  等待视频播放完毕,true等待/false不等待
     * @return
     */
    public boolean metahumanInsertVideo(String videoFilePath, String audioFilePath, boolean waitPlayVideo) {
        return boolCmd("metahumanInsertVideo", videoFilePath, audioFilePath, Boolean.toString(waitPlayVideo));
    }

    /**
     * 替换数字人背景
     *
     * @param bgFilePath   数字人背景 图片/视频 路径。仅替换绿幕背景的数字人模型
     * @param replaceRed   数字人背景的三通道之一的 R通道色值。默认-1 自动提取
     * @param replaceGreen 数字人背景的三通道之一的 G通道色值。默认-1 自动提取
     * @param replaceBlue  数字人背景的三通道之一的 B通道色值。默认-1 自动提取
     * @param simValue     相似度。 默认为0，此处参数用作微调RBG值。取值应当大于等于0
     * @return {Promise.<boolean>} 总是返回true。此函数依赖 initMetahuman函数运行，否则程序会崩溃
     */
    public boolean replaceBackground(String bgFilePath, int replaceRed, int replaceGreen, int replaceBlue, int simValue) {
        return boolCmd("replaceBackground", bgFilePath, Integer.toString(replaceRed), Integer.toString(replaceGreen), Integer.toString(replaceBlue), Integer.toString(simValue));
    }

    /**
     * 显示数字人说话的文本
     *
     * @param originY   第一个字显示的起始Y坐标点。 默认0 自适应高度
     * @param fontType  字体样式，支持操作系统已安装的字体。例如"Arial"、"微软雅黑"、"楷体"
     * @param fontSize  字体的大小。默认30
     * @param fontRed   字体颜色三通道之一的 R通道色值。默认可填入 128
     * @param fontGreen 字体颜色三通道之一的 G通道色值。默认可填入 255
     * @param fontBlue  字体颜色三通道之一的 B通道色值。默认可填入 0
     * @param italic    是否斜体,默认false
     * @param underline 是否有下划线,默认false
     * @return {Promise.<boolean>} 总是返回true。此函数依赖 initMetahuman函数运行，否则程序会崩溃
     */
    public boolean showSpeechText(int originY, String fontType, int fontSize, int fontRed, int fontGreen, int fontBlue, boolean italic, boolean underline) {
        return boolCmd("showSpeechText", Integer.toString(originY), fontType, Integer.toString(fontSize), Integer.toString(fontRed), Integer.toString(fontGreen), Integer.toString(fontBlue), Boolean.toString(italic), Boolean.toString(underline));
    }

    /**
     * 生成数字人短视频，此函数需要调用 initSpeechService 初始化语音服务
     *
     * @param saveVideoFolder, 保存的视频目录
     * @param text             要转换语音的文本
     * @param language         语言，参考开发文档 语言和发音人
     * @param voiceName        发音人，参考开发文档 语言和发音人
     * @param bgFilePath       数字人背景 图片/视频 路径，扣除绿幕会自动获取绿幕的RGB值，null 则不替换背景。仅替换绿幕背景的数字人模型
     * @param simValue         相似度，默认为0。此处参数用作绿幕扣除微调RBG值。取值应当大于等于0
     * @param voiceStyle       语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @param quality          音质，0低品质  1中品质  2高品质， 默认为0低品质
     * @param speechRate       语速，默认为0，取值范围 -100 至 200
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean showSpeechText(String saveVideoFolder, String text, String language, String voiceName, String bgFilePath, int simValue, String voiceStyle, int quality, int speechRate) {
        return boolCmd("showSpeechText", saveVideoFolder, text, language, voiceName, bgFilePath, Integer.toString(simValue), voiceStyle, Integer.toString(quality), Integer.toString(speechRate));
    }

    /**
     * 生成数字人短视频，此函数需要调用 initSpeechService 初始化语音服务
     *
     * @param {string} saveVideoFolder, 保存的视频目录
     * @param {string} text,要转换语音的文本
     * @param {string} language，语言，参考开发文档 语言和发音人
     * @param {string} voiceName，发音人，参考开发文档 语言和发音人
     * @param {string} bgFilePath,数字人背景 图片/视频 路径，扣除绿幕会自动获取绿幕的RGB值，null 则不替换背景。仅替换绿幕背景的数字人模型
     * @param {number} simValue, 相似度，默认为0。此处参数用作绿幕扣除微调RBG值。取值应当大于等于0
     * @param {string} voiceStyle，语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @param {number} quality，音质，0低品质  1中品质  2高品质， 默认为0低品质
     * @param {number} speechRate， 语速，默认为0，取值范围 -100 至 200
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean makeMetahumanVideo(String saveVideoFolder, String text, String language, String voiceName, String bgFilePath, int simValue, String voiceStyle, int quality, int speechRate) {
        if (StringUtils.isBlank(voiceStyle)) {
            voiceStyle = "General";
        }
        return boolCmd("makeMetahumanVideo", saveVideoFolder, text, language, voiceName, bgFilePath, Integer.toString(simValue), voiceStyle, Integer.toString(quality), Integer.toString(speechRate));
    }

    /**
     * 生成数字人说话文件，生成MP3文件和 lab文件，提供给 metahumanSpeechByFile 和使用
     *
     * @param {string} saveAudioPath, 保存的音频文件路径，扩展为.MP3格式。同名的 .lab文件需要和音频文件在同一目录下
     * @param {string} text,要转换语音的文本
     * @param {string} language，语言，参考开发文档 语言和发音人
     * @param {string} voiceName，发音人，参考开发文档 语言和发音人
     * @param {number} quality，音质，0低品质  1中品质  2高品质， 默认为0低品质
     * @param {number} speechRate， 语速，默认为0，取值范围 -100 至 200
     * @param {string} voiceStyle，语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean makeMetahumanSpeechFile(String saveAudioPath, String text, String language, String voiceName, int quality, int speechRate, String voiceStyle) {
        if (StringUtils.isBlank(voiceStyle)) {
            voiceStyle = "General";
        }
        return boolCmd("makeMetahumanVideo", saveAudioPath, text, language, voiceName, Integer.toString(quality), Integer.toString(speechRate), voiceStyle);
    }

    /**
     * 初始化数字人声音克隆服务
     *
     * @param {string} apiKey, API密钥
     * @param {string} voiceId, 声音ID
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean initSpeechCloneService(String apiKey, String voiceId) {
        return boolCmd("initSpeechCloneService", apiKey, voiceId);
    }

    /**
     * 数字人使用克隆声音说话，此函数需要调用 initSpeechCloneService 初始化语音服务
     *
     * @param {string}  saveAudioPath, 保存的发音文件路径。这里是路径，不是目录！
     * @param {string}  text,要转换语音的文本
     * @param {string}  language，语言，中文：zh-cn，其他语言：other-languages
     * @param {boolean} waitPlaySound，等待音频播报完毕，  true等待/false不等待
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean metahumanSpeechClone(String saveAudioPath, String text, String language, boolean waitPlaySound) {
        return boolCmd("metahumanSpeechClone", saveAudioPath, text, language, Boolean.toString(waitPlaySound));
    }

    /**
     * 使用克隆声音生成数字人短视频，此函数需要调用 initSpeechCloneService 初始化语音服务
     *
     * @param {string} saveVideoFolder, 保存的视频和音频文件目录
     * @param {string} text,要转换语音的文本
     * @param {string} language，语言，中文：zh-cn，其他语言：other-languages
     * @param {string} bgFilePath,数字人背景 图片/视频 路径，扣除绿幕会自动获取绿幕的RGB值，null 则不替换背景。仅替换绿幕背景的数字人模型
     * @param {number} simValue, 相似度，默认为0。此处参数用作绿幕扣除微调RBG值。取值应当大于等于0
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean makeMetahumanVideoClone(String saveVideoFolder, String text, String language, String bgFilePath, int simValue) {
        return boolCmd("makeMetahumanVideoClone", saveVideoFolder, text, language, bgFilePath, Integer.toString(simValue));
    }

    /**
     * 生成数字人说话文件(声音克隆)，生成MP3文件和 lab文件，提供给 metahumanSpeechByFile 和使用
     *
     * @param {string} saveAudioPath, 保存的发音文件路径。这里是路径，不是目录！
     * @param {string} text,要转换语音的文本
     * @param {string} language，语言，中文：zh-cn，其他语言：other-languages
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean makeMetahumanSpeechFileClone(String saveAudioPath, String text, String language) {
        return boolCmd("makeMetahumanSpeechFileClone", saveAudioPath, text, language);
    }

    /**
     * 获取WindowsDriver.exe 命令扩展参数，一般用作脚本远程部署场景，WindowsDriver.exe驱动程序传递参数给脚本服务端
     *
     * @return {Promise.<string>} 返回WindowsDriver驱动程序的命令行参数(不包含ip和port)
     */
    public String getExtendParam() {
        return strCmd("getExtendParam");
    }

    /**
     * 获取Windows ID
     *
     * @return {Promise.<string>} 成功返回Windows ID
     */
    public String getWindowsId() {
        return strCmd("getWindowsId");
    }

    /**
     * 切换新的人物形象动作，此函数无需训练数字人模型，直接切换各种人物形象动作和场景。
     *
     * @param {string} callApiKey, 调用函数的密钥
     * @param {string} actionVideoOrImage, 闭嘴的人物视频或者图片
     * @return {Promise.<boolean>} 成功返回true，失败返回false。调用不会立刻生效，加载完素材会自动切换
     */
    public boolean switchAction(String callApiKey, String actionVideoOrImage) {
        return boolCmd("switchAction", callApiKey, actionVideoOrImage);
    }

    /**
     * 训练数字人，训练时长为10-30分钟
     *
     * @param {string} callApiKey, 调用函数的密钥
     * @param {string} trainVideoOrImagePath, 闭嘴的人物视频或者图片 素材
     * @param {string} srcMetahumanModelPath, 预训练数字人模型路径
     * @param {string} saveHumanModelFolder, 保存训练完成的模型目录
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    public boolean trainHumanModel(String callApiKey, String trainVideoOrImagePath, String srcMetahumanModelPath, String saveHumanModelFolder) {
        return boolCmd("trainHumanModel", callApiKey, trainVideoOrImagePath, srcMetahumanModelPath, saveHumanModelFolder);
    }

    /**
     * 切换声音克隆模型
     *
     * @param {string} cloneServerIp, 克隆声音服务端
     * @param {string} gptWeightsPath, gpt 模型权重路径。指克隆服务所在的电脑/服务器 路径
     * @param {string} sovitsWeightsPath, sovits 模型权重路径。指克隆服务所在的电脑/服务器 路径
     * @return {Promise.<boolean>} 失败返回false,成功返回true。 切换到与原模型无关音色的模型，切记更换参考音频和文本
     */
    public boolean switchCloneAudioModel(String cloneServerIp, String gptWeightsPath, String sovitsWeightsPath) {
        return this.boolCmd("switchCloneAudioModel", cloneServerIp, gptWeightsPath, sovitsWeightsPath);
    }

    /**
     * 重启声音克隆服务
     *
     * @param {string} cloneServerIp, 克隆声音服务端
     * @return {Promise.<boolean>} 失败返回false,成功返回true。重启服务会中断连接，实际并未准确返回值。重启后模型加载需要时间，调用此函数需显示等待几秒，再去访问声音克隆服务
     */
    public boolean restartCloneAudioServer(String cloneServerIp) {
        return this.boolCmd("restartCloneAudioServer", cloneServerIp);
    }

    /**
     * 克隆声音，需要部署服务端
     *
     * @param {string} cloneServerIp, 克隆声音服务端
     * @param {string} saveAudioPath, 保存克隆声音的路径
     * @param {string} referAudioPath, 参考音频路径，3-10秒，音频时长不能大于等于10秒
     * @param {string} referText, 参考音频对应的文本
     * @param {string} cloneText, 要克隆的文本
     * @param {number} speedFactor, 语速（0.5为半速，1.0为正常速度，1.5为1.5倍速，以此类推）。默认为1.0 正常语速
     * @return {Promise.<boolean>} 失败返回false,成功返回true
     */
    public boolean makeCloneAudio(String cloneServerIp, String saveAudioPath, String referAudioPath, String referText, String cloneText, float speedFactor) {
        if (speedFactor <= 0) {
            speedFactor = 1.0F;
        }
        return this.boolCmd("makeCloneAudio", cloneServerIp, saveAudioPath, referAudioPath, referText, cloneText, String.valueOf(speedFactor));
    }

    /**
     * 播报音频文件
     *
     * @param {string}  audioPath, 音频文件路径
     * @param {boolean} isWait, 是否等待.为true时,等待播放完毕
     * @return {Promise.<boolean>} 失败返回false,成功返回true
     */
    public boolean playAudio(String audioPath, boolean isWait) {
        return this.boolCmd("playAudio", audioPath, Boolean.toString(isWait));
    }

    /**
     * 播报音频文件(EX)，playAudioEx 不能与 metahumanSpeechByFileEx 同步执行
     *
     * @param {string}  audioPath, 音频文件路径
     * @param {boolean} enableRandomParam, 是否启用随机去重参数
     * @param {boolean} isWait, 是否等待.为true时,等待播放完毕
     * @return {Promise.<boolean>} 总是返回true，函数仅添加播放音频文件到队列不处理返回
     */
    public boolean playAudioEx(String audioPath, boolean enableRandomParam, boolean isWait) {
        return this.boolCmd("playAudioEx", audioPath, Boolean.toString(enableRandomParam), Boolean.toString(isWait));
    }

    /**
     * 播报视频文件
     *
     * @param {string}  videoPath, 视频文件路径 (多个视频切换播放 视频和音频编码必须一致)
     * @param {number}  videoSacle, 视频缩放（0.5缩小一半，1.0为原始大小）
     * @param {boolean} isLoopPlay, 是否循环播放
     * @param {boolean} enableRandomParam, 是否启用随机去重参数
     * @param {boolean} isWait, 是否等待播报完毕。 值为false时，不等待播放结束。未播报结束前再次调用此函数 会终止前面的播报内容
     * @return {Promise.<boolean>} 失败返回false,成功返回true。
     */
    public boolean playMedia(String videoPath, float videoSacle, boolean isLoopPlay, boolean enableRandomParam, boolean isWait) {
        if (videoSacle <= 0) {
            videoSacle = 1.0F;
        }
        return this.boolCmd("playMedia", videoPath, Float.toString(videoSacle), Boolean.toString(isLoopPlay), Boolean.toString(enableRandomParam), Boolean.toString(isWait));
    }

    /**
     * 调节 playMedia 音量大小(底层用的内存共享，支持多进程控制)
     *
     * @param {number} volumeScale, 音量缩放（0.5调低一半，1.0为原始音量大小）。默认为原始大小
     * @return {Promise.<boolean>} 失败返回false,成功返回true。
     */
    public boolean setMediaVolumeScale(float volumeScale) {
        return this.boolCmd("setMediaVolumeScale", Float.toString(volumeScale));
    }

    /**
     * 生成lab文件，需要部署服务端
     *
     * @param {string} labServerIp, lab服务端IP
     * @param {string} audioPath, 音频文件
     * @return {Promise.<boolean>} 失败返回false,成功返回true 并生成 与 audioPath 同目录下的 .lab 后缀文件。(音频文件+lab文件可以直接驱动数字人)
     */
    public boolean makeCloneLab(String labServerIp, String audioPath) {
        return this.boolCmd("makeCloneLab", labServerIp, audioPath);
    }

    /**
     * 语音识别，需要部署服务端
     *
     * @param {string} labServerIp, lab服务端IP
     * @param {string} audioPath, 音频文件
     * @return {Promise.<boolean>} 失败返回null, 成功返回识别到的内容
     */
    public boolean cloneAudioToText(String labServerIp, String audioPath) {
        return this.boolCmd("cloneAudioToText", labServerIp, audioPath);
    }

    /**
     * 关闭驱动
     *
     * @return boolean
     */
    public boolean closeDriver() {
        return boolCmd("closeDriver");
    }

    // ==================== HID 相关方法 ====================

    /**
     * 初始化 HID（硬件输入设备）
     *
     * 用于与 Android 设备通过 USB 进行直接交互。
     * 此方法应在 AndroidBot.initHid() 之前调用。
     *
     * @return boolean 成功返回 true，失败返回 false
     */
    public boolean initHid() {
        return this.boolCmd("initHid");
    }

    /**
     * 获取 HID 数据
     *
     * 获取初始化后的 HID 设备数据，用于 AndroidBot 验证初始化是否成功。
     *
     * @return String HID 设备数据数组（使用 "|" 分隔的设备 ID）
     */
    public String getHidData() {
        return this.strCmd("getHidData");
    }

    /**
     * HID 按下操作（由 AndroidBot 调用）
     *
     * @param androidId 安卓设备 ID
     * @param angle     屏幕旋转角度
     * @param x         横坐标
     * @param y         纵坐标
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidPress(String androidId, int angle, int x, int y) {
        return this.strCmd("hidPress", androidId, Integer.toString(angle), Integer.toString(x), Integer.toString(y));
    }

    /**
     * HID 移动操作
     *
     * @param androidId 安卓设备 ID
     * @param angle     屏幕旋转角度
     * @param x         横坐标
     * @param y         纵坐标
     * @param duration  移动时长，单位毫秒
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidMove(String androidId, int angle, int x, int y, int duration) {
        return this.strCmd("hidMove", androidId, Integer.toString(angle), Integer.toString(x), Integer.toString(y), Integer.toString(duration));
    }

    /**
     * HID 释放操作
     *
     * @param androidId 安卓设备 ID
     * @param angle     屏幕旋转角度
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidRelease(String androidId, int angle) {
        return this.strCmd("hidRelease", androidId, Integer.toString(angle));
    }

    /**
     * HID 单击操作
     *
     * @param androidId 安卓设备 ID
     * @param angle     屏幕旋转角度
     * @param x         横坐标
     * @param y         纵坐标
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidClick(String androidId, int angle, int x, int y) {
        return this.strCmd("hidClick", androidId, Integer.toString(angle), Integer.toString(x), Integer.toString(y));
    }

    /**
     * HID 双击操作
     *
     * @param androidId 安卓设备 ID
     * @param angle     屏幕旋转角度
     * @param x         横坐标
     * @param y         纵坐标
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidDoubleClick(String androidId, int angle, int x, int y) {
        return this.strCmd("hidDoubleClick", androidId, Integer.toString(angle), Integer.toString(x), Integer.toString(y));
    }

    /**
     * HID 长按操作
     *
     * @param androidId 安卓设备 ID
     * @param angle     屏幕旋转角度
     * @param x         横坐标
     * @param y         纵坐标
     * @param duration  长按时长，单位毫秒
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidLongClick(String androidId, int angle, int x, int y, int duration) {
        return this.strCmd("hidLongClick", androidId, Integer.toString(angle), Integer.toString(x), Integer.toString(y), Integer.toString(duration));
    }

    /**
     * HID 滑动操作
     *
     * @param androidId 安卓设备 ID
     * @param angle     屏幕旋转角度
     * @param startX    起始横坐标
     * @param startY    起始纵坐标
     * @param endX      结束横坐标
     * @param endY      结束纵坐标
     * @param duration  滑动时长，单位毫秒
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidSwipe(String androidId, int angle, int startX, int startY, int endX, int endY, int duration) {
        return this.strCmd("hidSwipe", androidId, Integer.toString(angle), Integer.toString(startX), Integer.toString(startY), Integer.toString(endX), Integer.toString(endY), Integer.toString(duration));
    }

    /**
     * HID 手势操作
     *
     * @param androidId   安卓设备 ID
     * @param angle       屏幕旋转角度
     * @param gesturePath 手势路径
     * @param duration    手势时长，单位毫秒
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidDispatchGesture(String androidId, int angle, Object gesturePath, int duration) {
        return this.strCmd("hidDispatchGesture", androidId, Integer.toString(angle), gesturePath.toString(), Integer.toString(duration));
    }

    /**
     * HID 多手势操作
     *
     * @param androidId    安卓设备 ID
     * @param angle        屏幕旋转角度
     * @param gesturesPath 多个手势路径
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidDispatchGestures(String androidId, int angle, Object gesturesPath) {
        return this.strCmd("hidDispatchGestures", androidId, Integer.toString(angle), gesturesPath.toString());
    }

    /**
     * HID 返回键
     *
     * @param androidId 安卓设备 ID
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidBack(String androidId) {
        return this.strCmd("hidBack", androidId);
    }

    /**
     * HID Home 键
     *
     * @param androidId 安卓设备 ID
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidHome(String androidId) {
        return this.strCmd("hidHome", androidId);
    }

    /**
     * HID 显示最近任务
     *
     * @param androidId 安卓设备 ID
     * @return String 成功返回 "true"，失败返回 "false"
     */
    public String hidRecents(String androidId) {
        return this.strCmd("hidRecents", androidId);
    }

}
