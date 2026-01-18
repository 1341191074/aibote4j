package net.aibote.sdk;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.aibote.sdk.dto.OCRResult;
import net.aibote.sdk.dto.Point;
import net.aibote.sdk.options.Mode;
import net.aibote.sdk.options.Region;
import net.aibote.sdk.options.SubColor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class WebBot extends AbstractPlatformBot {

    @Override
    public String getPlatformName() {
        return "Web";
    }

    @Override
    protected void platformInitialize() {
        // Web平台特定的初始化
    }

    @Override
    protected void platformCleanup() {
        // Web平台特定的清理
    }

    /**
     * 导航至 url
     *
     * @param url 网址
     * @return boolean
     */
    public boolean navigate(String url) {
        return booleanCmd("goto", url);
    }

    /**
     * 新建tab页面并跳转到指定url
     *
     * @param url 网址
     * @return boolean
     */
    public boolean newPage(String url) {
        return booleanCmd("newPage", url);
    }

    /**
     * 返回
     *
     * @return boolean
     */
    public boolean back() {
        return booleanCmd("back");
    }

    /**
     * 前进
     *
     * @return boolean
     */
    public boolean forward() {
        return booleanCmd("forward");
    }


    /**
     * 刷新
     *
     * @return boolean
     */
    public boolean refresh() {
        return booleanCmd("refresh");
    }

    /**
     * 获取当前页面id
     *
     * @return String
     */
    public String getCurPageId() {
        return strCmd("getCurPageId");
    }

    /**
     * 获取所有页面id
     *
     * @return String
     */
    public String getAllPageId() {
        return strCmd("getAllPageId");
    }


    /**
     * 切换指定页面
     *
     * @return boolean
     */
    public boolean switchPage(String pageId) {
        return booleanCmd("switchPage", pageId);
    }

    /**
     * 关闭当前页面
     *
     * @return boolean
     */
    public boolean closePage() {
        return booleanCmd("closePage");
    }

    /**
     * 获取当前url
     *
     * @return String
     */
    public String getCurrentUrl() {
        return strCmd("getCurrentUrl");
    }


    /**
     * 获取当前标题
     *
     * @return String
     */
    public String getTitle() {
        return strCmd("getTitle");
    }

    /**
     * 切换frame
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public boolean switchFrame(String xpath) {
        return booleanCmd("switchFrame", xpath);
    }


    /**
     * 切换到主frame
     *
     * @return boolean
     */
    public boolean switchMainFrame() {
        return booleanCmd("switchMainFrame");
    }

    /**
     * 点击元素
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public boolean clickElement(String xpath) {
        return booleanCmd("clickElement", xpath);
    }

    /**
     * 设置编辑框值
     *
     * @param xpath xpath路径
     * @param value 目标值
     * @return boolean
     */
    public boolean setElementValue(String xpath, String value) {
        return booleanCmd("setElementValue", xpath, value);
    }


    /**
     * 获取文本
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public String getElementText(String xpath) {
        return strCmd("getElementText", xpath);
    }


    /**
     * 获取outerHTML
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public String getElementOuterHTML(String xpath) {
        return strCmd("getElementOuterHTML", xpath);
    }


    /**
     * 获取innerHTML
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public String getElementInnerHTML(String xpath) {
        return strCmd("getElementInnerHTML", xpath);
    }

    /**
     * 设置属性值
     *
     * @param xpath xpath路径
     * @param value 属性值
     * @return boolean
     */
    public boolean setElementAttribute(String xpath, String value) {
        return booleanCmd("setElementAttribute", xpath, value);
    }

    /**
     * 获取指定属性的值
     *
     * @param xpath     xpath路径
     * @param attribute 属性名
     * @return boolean
     */
    public String getElementAttribute(String xpath, String attribute) {
        return strCmd("getElementAttribute", xpath, attribute);
    }


    /**
     * 获取矩形位置
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public String getElementRect(String xpath) {
        return strCmd("getElementRect", xpath);
    }


    /**
     * 判断元素是否选中
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public boolean isSelected(String xpath) {
        return booleanCmd("isSelected", xpath);
    }


    /**
     * 判断元素是否可见
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public boolean isDisplayed(String xpath) {
        return booleanCmd("isDisplayed", xpath);
    }

    /**
     * 判断元素是否可用
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public boolean isEnabled(String xpath) {
        return booleanCmd("isEnabled", xpath);
    }

    /**
     * 清空元素
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public boolean clearElement(String xpath) {
        return booleanCmd("clearElement", xpath);
    }

    /**
     * 设置元素焦点
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public boolean setElementFocus(String xpath) {
        return booleanCmd("setElementFocus", xpath);
    }

    /**
     * 通过元素上传文件
     *
     * @param xpath       xpath路径
     * @param uploadFiles 上传的文件路径
     * @return boolean
     */
    public boolean uploadFile(String xpath, String uploadFiles) {
        return booleanCmd("uploadFile", xpath, uploadFiles);
    }

    /**
     * 输入文本
     *
     * @param xpath xpath路径
     * @param txt   文本内容
     * @return boolean
     */
    public boolean sendKeys(String xpath, String txt) {
        return booleanCmd("sendKeys", xpath, txt);
    }


    /**
     * 发送Vk虚拟键
     *
     * @param vk 虚拟键
     * @return boolean
     */
    public boolean sendVk(String vk) {
        return booleanCmd("sendVk", vk);
    }

    /**
     * 单击鼠标
     *
     * @param x   x 横坐标，非Windows坐标，页面左上角为起始坐标
     * @param y   y 纵坐标，非Windows坐标，页面左上角为起始坐标
     * @param opt 功能键。单击左键:1  单击右键:2  按下左键:3  弹起左键:4  按下右键:5  弹起右键:6  双击左键：7
     * @return boolean
     */
    public boolean clickMouse(String x, String y, String opt) {
        return booleanCmd("clickMouse", x, y, opt);
    }

    /**
     * 移动鼠标
     *
     * @param x x 横坐标，非Windows坐标，页面左上角为起始坐标
     * @param y y 纵坐标，非Windows坐标，页面左上角为起始坐标
     * @return boolean
     */
    public boolean moveMouse(String x, String y) {
        return booleanCmd("moveMouse", x, y);
    }

    /**
     * 滚动鼠标
     *
     * @param deltaX deltaX 水平滚动条移动的距离
     * @param deltaY deltaY 垂直滚动条移动的距离
     * @param x      可选参数，鼠标横坐标位置， 默认为0
     * @param y      可选参数，鼠标纵坐标位置， 默认为0
     * @return boolean
     */
    public boolean wheelMouse(String deltaX, String deltaY, String x, String y) {
        if (StringUtils.isBlank(x)) {
            x = "0";
        }
        if (StringUtils.isBlank(y)) {
            y = "0";
        }
        return booleanCmd("wheelMouse", deltaX, deltaY, x, y);
    }

    /**
     * 通过xpath 点击鼠标
     *
     * @param xpath xpath路径
     * @param opt   功能键。单击左键:1  单击右键:2  按下左键:3  弹起左键:4  按下右键:5  弹起右键:6  双击左键：7
     * @return
     */
    public boolean clickMouseByXpath(String xpath, String opt) {
        return booleanCmd("clickMouseByXpath", xpath, opt);
    }

    /**
     * xpath移动鼠标(元素中心点)
     *
     * @param xpath xpath路径
     * @return boolean
     */
    public boolean moveMouseByXpath(String xpath) {
        return booleanCmd("moveMouseByXpath", xpath);
    }

    /**
     * xpath滚动鼠标
     *
     * @param xpath  元素路径
     * @param deltaX 水平滚动条移动的距离
     * @param deltaY 垂直滚动条移动的距离
     * @return boolean
     */
    public boolean wheelMouseByXpath(String xpath, String deltaX, String deltaY) {
        return booleanCmd("wheelMouseByXpath", xpath, deltaX, deltaY);
    }


    /**
     * 截图
     *
     * @param xpath 可选参数，元素路径。如果指定该参数则截取元素图片
     * @return String
     */
    public String takeScreenshot(String xpath) {
        if (StringUtils.isBlank(xpath)) {
            return strCmd("takeScreenshot");
        }
        return strCmd("takeScreenshot", xpath);
    }


    /**
     * 点击警告框
     *
     * @param acceptOrCancel true接受, false取消
     * @param promptText     可选参数，输入prompt警告框文本
     * @return boolean
     */
    public boolean clickAlert(boolean acceptOrCancel, String promptText) {
        return booleanCmd("clickAlert", Boolean.toString(acceptOrCancel), promptText);
    }


    /**
     * 截图
     *
     * @return String
     */
    public String getAlertText() {
        return strCmd("getAlertText");
    }

    /**
     * 获取指定url匹配的cookies
     *
     * @param url 指定的url http://或https:// 起头
     * @return 成功返回json格式的字符串，失败返回null
     */
    public String getCookies(String url) {
        return strCmd("getCookies", url);
    }

    /**
     * 获取指定url匹配的cookies
     *
     * @return 成功返回json格式的字符串，失败返回null
     */
    public String getAllCookies() {
        return strCmd("getAllCookies");
    }

    public boolean setCookie(String name, String value, String url) {
        String domain = "", path = "", sameSite = "", priority = "", sourceScheme = "", partitionKey = "";
        boolean secure = false, httpOnly = false, sameParty = false;
        int expires = 0, sourcePort = 0;
        return setCookie(name, value, url, domain, path, secure, httpOnly, sameSite, expires, priority, sameParty, sourceScheme, sourcePort, partitionKey);
    }

    public boolean setCookie(String name, String value, String url, String domain) {
        String path = "", sameSite = "", priority = "", sourceScheme = "", partitionKey = "";
        boolean secure = false, httpOnly = false, sameParty = false;
        int expires = 0, sourcePort = 0;
        return setCookie(name, value, url, domain, path, secure, httpOnly, sameSite, expires, priority, sameParty, sourceScheme, sourcePort, partitionKey);
    }

    public boolean setCookie(String name, String value, String url, String domain, Map<String, String> options) {
        String path = "", sameSite = "", priority = "", sourceScheme = "", partitionKey = "";
        boolean secure = false, httpOnly = false, sameParty = false;
        int expires = 0, sourcePort = 0;
        if (StringUtils.isBlank(options.get("path"))) {
            path = options.get("path");
        }
        if (StringUtils.isBlank(options.get("sameSite"))) {
            sameSite = options.get("sameSite");
        }
        if (StringUtils.isBlank(options.get("priority"))) {
            priority = options.get("priority");
        }
        if (StringUtils.isBlank(options.get("sourceScheme"))) {
            sourceScheme = options.get("sourceScheme");
        }
        if (StringUtils.isBlank(options.get("partitionKey"))) {
            partitionKey = options.get("partitionKey");
        }
        if (StringUtils.isBlank(options.get("secure"))) {
            if ("true".equals(options.get("secure"))) {
                secure = true;
            }
        }
        if (StringUtils.isBlank(options.get("httpOnly"))) {
            if ("true".equals(options.get("httpOnly"))) {
                httpOnly = true;
            }
        }
        if (StringUtils.isBlank(options.get("sameParty"))) {
            if ("sameParty".equals(options.get("sameParty"))) {
                sameParty = true;
            }
        }
        if (StringUtils.isNumeric(options.get("expires"))) {
            expires = Integer.parseInt(options.get("expires"));
        }
        if (StringUtils.isNumeric(options.get("sourcePort"))) {
            sourcePort = Integer.parseInt(options.get("sourcePort"));
        }
        return setCookie(name, value, url, domain, path, secure, httpOnly, sameSite, expires, priority, sameParty, sourceScheme, sourcePort, partitionKey);
    }

    /**
     * 设置cookie  name、value和url必填参数，其他参数可选
     *
     * @param name         String
     * @param value        String
     * @param url          String
     * @param domain       String
     * @param path         String
     * @param secure       boolean
     * @param httpOnly     boolean
     * @param sameSite     String
     * @param expires      String
     * @param priority     String
     * @param sameParty    boolean
     * @param sourceScheme String
     * @param sourcePort   String
     * @param partitionKey String
     * @return
     */
    public boolean setCookie(String name, String value, String url, String domain, String path, boolean secure, boolean httpOnly, String sameSite, int expires, String priority, boolean sameParty, String sourceScheme, int sourcePort, String partitionKey) {
        return booleanCmd("setCookie", name, value, url, domain, path, Boolean.toString(secure), Boolean.toString(httpOnly), sameSite, Integer.toString(expires), priority, Boolean.toString(sameParty), sourceScheme, Integer.toString(sourcePort), partitionKey);
    }

    /**
     * 删除指定cookies
     *
     * @param name 要删除的 Cookie 的名称。
     * @return boolean
     */
    public boolean deleteCookies(String name) {
        return booleanCmd("deleteCookies", name);
    }

    /**
     * 删除指定cookies
     *
     * @param name 要删除的 Cookie 的名称。
     * @param url  url
     * @return boolean
     */
    public boolean deleteCookies(String name, String url) {
        return booleanCmd("deleteCookies", name, url);
    }

    /**
     * 删除指定cookies
     *
     * @param name 要删除的 Cookie 的名称。
     * @param url  url
     * @return boolean
     */
    public boolean deleteCookies(String name, String url, String domain) {
        return booleanCmd("deleteCookies", name, url, domain);
    }

    /**
     * 删除指定cookies
     *
     * @param name 要删除的 Cookie 的名称。
     * @param url  url
     * @return boolean
     */
    public boolean deleteCookies(String name, String url, String domain, String path) {
        return booleanCmd("deleteCookies", name, url, domain, path);
    }

    /**
     * 删除所有cookies
     *
     * @return boolean
     */
    public boolean deleteAllCookies() {
        return booleanCmd("deleteAllCookies");
    }

    /**
     * 注入JavaScript <br />
     * 假如注入代码为函数且有return语句，则返回retrun 的值，否则返回null;  注入示例：(function () {return "aibote rpa"})();
     *
     * @param command 注入的js代码
     * @return
     */
    public String executeScript(String command) {
        return strCmd("executeScript", command);
    }

    /**
     * 获取窗口位置和状态 <br />
     * 成功返回矩形位置和窗口状态，失败返回null
     *
     * @return {left:number, top:number, width:number, height:number, windowState:string}
     */
    public String getWindowPos() {
        return strCmd("getWindowPos");
    }

    /**
     * 设置窗口位置和状态
     *
     * @param windowState 窗口状态，正常:"normal"  最小化:"minimized"  最大化:"maximized"  全屏:"fullscreen"
     * @param left        可选参数，浏览器窗口位置，此参数仅windowState 值为 "normal" 时有效
     * @param top         可选参数，浏览器窗口位置，此参数仅windowState 值为 "normal" 时有效
     * @param width       可选参数，浏览器窗口位置，此参数仅windowState 值为 "normal" 时有效
     * @param height      可选参数，浏览器窗口位置，此参数仅windowState 值为 "normal" 时有效
     * @return
     */
    public boolean setWindowPos(String windowState, float left, float top, int width, float height) {
        return booleanCmd("setWindowPos", Float.toString(left), Float.toString(top), Float.toString(width), Float.toString(height));
    }

    /**
     * 获取WebDriver.exe 命令扩展参数，一般用作脚本远程部署场景，WebDriver.exe驱动程序传递参数给脚本服务端
     *
     * @return String 返回WebDriver 驱动程序的命令行["extendParam"] 字段的参数
     */
    public String getExtendParam() {
        return strCmd("getExtendParam");
    }

    /**
     * 手机浏览器仿真
     *
     * @param width           宽度
     * @param height          高度
     * @param userAgent       用户代理
     * @param platform        系统，例如 "Android"、"IOS"、"iPhone"
     * @param platformVersion 系统版本号，例如 "9.0"，应当与userAgent提供的版本号对应
     * @param acceptLanguage  可选参数 - 语言，例如 "zh-CN"、"en"
     * @param timezoneId      可选参数 - 时区，时区标识，例如"Asia/Shanghai"、"Europe/Berlin"、"Europe/London" 时区应当与 语言、经纬度 对应
     * @param latitude        可选参数 - 纬度，例如 31.230416
     * @param longitude       可选参数 - 经度，例如 121.473701
     * @param accuracy        可选参数 - 准确度，例如 1111
     * @return boolean
     */
    public boolean mobileEmulation(int width, int height, String userAgent, String platform, String platformVersion, String acceptLanguage, String timezoneId, float latitude, float longitude, float accuracy) {
        return booleanCmd("mobileEmulation", Integer.toString(width), Integer.toString(height), userAgent, platform, platformVersion, acceptLanguage, timezoneId, Float.toString(latitude), Float.toString(longitude), Float.toString(accuracy));
    }

    /**
     * 关闭浏览器
     *
     * @return boolean
     */
    public boolean closeBrowser() {
        return booleanCmd("closeBrowser");
    }

    /**
     * 关闭WebDriver.exe驱动程序
     *
     * @return boolean
     */
    public boolean closeDriver() {
        return booleanCmd("closeDriver");
    }

}

