import net.aibote.sdk.WebBot;

import net.aibote.server.WebBotServer;

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
