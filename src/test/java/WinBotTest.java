import net.aibote.sdk.WinBot;

public class WinBotTest extends WinBot {

    public static void main(String[] args) {

        WinBot.startServer(WinBotTest.class, "127.0.0.1", 26678, "E:\\aibote\\Aibote\\");


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


        String notepad = this.findWindow("Notepad", "");
        System.out.println(notepad);


       // this.closeDriver();
    }
}
