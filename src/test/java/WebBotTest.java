import net.aibote.sdk.WebBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebBotTest extends WebBot {
    private static final Logger log = LoggerFactory.getLogger(WebBotTest.class);

    public static void main(String[] args) {
//        Map<String, String> options = new HashMap<>();
//        options.put("debugPort", "9223");
//        WebBot.startServer(WebBotTest.class, "127.0.0.1", 19028, options);

        //WebBot.startServer(test, "E:\\aibote\\Aibote\\");

    }

    //模拟远程启动
    //WebDriver.exe "{\"serverIp\":\"127.0.0.1\",\"serverPort\":18023,\"browserName\":\"chrome\",\"debugPort\":9223,\"browserPath\":\"null\",\"argument\":\"null\",\"userDataDir\":\"null\",\"extendParam\":\"\"}"
    @Override
    public void webMain() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        boolean ret = this.navigate("http://www.baidu.com");//url必须带http://
        log.info(String.valueOf(ret));
        String curPageId = this.getCurPageId();
        log.info(curPageId);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String base64 = this.takeScreenshot(null);
        log.info(base64);
        //this.closeBrowser(); //关闭浏览器时，driver会一同关闭
        //this.closeDriver();
    }
}
