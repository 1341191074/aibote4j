import net.aibote.sdk.WinBot;
import net.aibote.sdk.dto.OCRResult;
import net.aibote.sdk.options.Mode;
import net.aibote.sdk.options.Region;
import net.aibote.server.WinBoteServer;

import java.util.List;

public class WinBotTest extends WinBot {

    public static void main(String[] args) {
        //WinBot.startServer(WinBotTest.class, "127.0.0.1", 26678, "E:\\aibote\\Aibote\\");
        WinBoteServer winBoteServer = new WinBoteServer();
        winBoteServer.runLocalClient(19029, null);
        winBoteServer.startServer(WinBotTest.class, 19029);
    }

    @Override
    public void webMain() {
        this.sleep(5000); //启动后静默5秒
        System.out.println("1111111111111111");
        boolean b = this.initOcr("192.168.109.1", 9527, false, true, false);
        //boolean b = this.initOcr("127.0.0.1", 9527, false, false, false);
        System.out.println("====" + b);
        Region region = new Region();
        region.left = 0;
        region.top = 1;
        region.right = 1153;
        region.bottom = 637;
        List<OCRResult> list = this.ocrByHwnd("722978", region, 5, 0, 0, Mode.front);
        StringBuilder words = new StringBuilder();
        if (list != null) {
            list.forEach((obj) -> {
                words.append(obj.word).append("\n");
            });
            System.out.println(words);
        }
        System.out.println("------------------");
//        String elementHwnd = this.getElementWindow("525472", "Window/Edit");
//        System.out.println("elementHwnd = " + elementHwnd); //应返回 526026
//        this.clickMouse("525472", 389, 73, 1, Mode.backed, elementHwnd);
//        this.sendKeysByHwnd(elementHwnd, "中文测试");
//        this.sendKeysByHwnd(elementHwnd, "en test");

//        String myconf = (String) super.ymlConfig.get("myconf");
//        System.out.println(myconf);
//
//        String notepad = this.findWindow("Notepad", "");
//        System.out.println(notepad);
//        String elementWindow = this.getElementWindow(notepad, "Window/Edit");
//        this.sendKeysByHwnd(elementWindow, "最小化发送测试");
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//        }


        //this.closeDriver();
    }
}
