import net.aibote.sdk.WinBot;
import net.aibote.server.WinBoteServer;

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
//        this.initOcr("127.0.0.1", 9528);
//        Region region = new Region();
//        region.left = 0;
//        region.top = 216;
//        region.right = 1327;
//        region.bottom = 1137;
//        List<OCRResult> list = this.ocr("5180720", region, 5, 0, 0, Mode.front);
//        StringBuilder words = new StringBuilder();
//        if (list != null) {
//            list.forEach((obj) -> {
//                words.append(obj.word).append("\n");
//            });
//            System.out.println(words);
//        }

//        String elementHwnd = this.getElementWindow("525472", "Window/Edit");
//        System.out.println("elementHwnd = " + elementHwnd); //应返回 526026
//        this.clickMouse("525472", 389, 73, 1, Mode.backed, elementHwnd);
//        this.sendKeysByHwnd(elementHwnd, "中文测试");
//        this.sendKeysByHwnd(elementHwnd, "en test");

        String myconf = (String) super.ymlConfig.get("myconf");
        System.out.println(myconf);

        String notepad = this.findWindow("Notepad", "");
        System.out.println(notepad);
        String elementWindow = this.getElementWindow(notepad, "Window/Edit");
        this.sendKeysByHwnd(elementWindow, "最小化发送测试");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        // this.closeDriver();
    }
}
