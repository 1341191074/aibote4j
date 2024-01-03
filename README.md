# Aibote4j
aibote java版本sdk封装 ， 基于jdk21
### By Reach(QQ:1341191074)   Aibote4j交流Q群：496086899
github: https://github.com/1341191074/aibote4j  
<br />

## 安卓/windows/网页web 三大模拟操作全能型自动化
```
使用方式见   
WebBotTest.java 
WinBotTest.java
```
<br />

``` 
Aibote是江西爱伯特科技自主研发的一款纯代码RPA办公自动化框架，支持Android、Browser和Windows 三大平台。框架免费、API和接口协议开源，个人、企业商用零费用
以socket tcp接口协议通信方式命令驱动，支持任何一门计算机语言调用。

Aibote能力：
    1、AndroidBot，底层自主研发，支持安卓原生APP和H5界面元素和图色定位。元素元素定位速度是Appium框架的的10倍，2340*1080 图色定位仅需要50毫秒！
    2、WindowsBot，底层自主研发，支持Windows应用、.NET、WinForm、WPF、QT、JAVA(Swing和AWT等GUI库)和Electron 等语言开发的窗口界面元素和图色定位，独家xpath算法 简洁急速，
    元素/图色定位速度分别是可视化RPA的3倍和20倍！
    3、WebBot，底层自主研发，支持chromium内核的所有浏览器和应用。C/C++语言基于浏览器内核协议研发而成的一款web自动化框架。速度是selenium 10倍！
    4、Android远程投屏，底层自主研发，可在一台电脑监控观察多台安卓RPA机器人运行状态并批量管理操作
    5、自建OCR服务器，支持文字识别和定位，免费且不限制使用次数！
    6、自研AiboteScriptUI界面开发工具，提供人机交互功能，打包exe发布机器人可以在离线环境运行！
```
<br />

使用参考：
``` JAVA
public class WebBotTest extends WebBot {

    public static void main(String[] args) {
        WebBotServer webBotServer = new WebBotServer();
        webBotServer.runLocalClient(19028, null, 0, null, null, null, null);
        webBotServer.startServer(WebBotTest.class, 19028);
    }

    //模拟远程启动
    //WebDriver.exe "{\"serverIp\":\"127.0.0.1\",\"serverPort\":19028,\"browserName\":\"chrome\",\"debugPort\":9223,\"browserPath\":\"null\",\"argument\":\"null\",\"userDataDir\":\"null\",\"extendParam\":\"\"}"
    @Override
    public void webMain() {
        this.sleep(5000);

        boolean ret = this.navigate("https://www.bilibili.com/");//url必须带http://
        log.info(String.valueOf(ret));

        String curPageId = null;
        curPageId = this.getCurPageId();
        log.info("第一次获取pageId : " + curPageId);

        this.sendKeys("//*[@id=\"nav-searchform\"]/div[1]/input", "aibote");
        this.sleep(5000);

        this.clickElement("//*[@id=\"nav-searchform\"]/div[2]");
        this.sleep(5000);

        curPageId = this.getCurPageId();
        log.info("第二次获取pageId : " + curPageId);

        String myconf = (String) super.ymlConfig.get("myconf");
        System.out.println(myconf);

//        this.switchPage(curPageId);
//        this.clickElement("//*[@id=\"nav-searchform\"]/div[2]");
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//        }

        //String base64 = this.takeScreenshot(null);
        //log.info(base64);
        //this.closeBrowser(); //关闭浏览器时，driver会一同关闭
        //this.closeDriver();
    }
}
```

changelog生成
git log --date=format:"%Y-%m-%d" --pretty="- %cd %an %s%n`````` %n%b%n``````" > CHANGELOG.md


```text
免责声明
    仅供用于学习和交流。
    使用者请勿使用本框架编写商业、违法、等有损他人利益的软件或插件等。使用本框架造成的后果全部由使用者自负，与本人无关。
    本人保留本免责声明的最终解释权。
```


