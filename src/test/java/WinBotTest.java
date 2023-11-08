import net.aibote.utils.WinBot;
import net.aibote.utils.dto.OCRResult;
import net.aibote.utils.options.Mode;
import net.aibote.utils.options.Region;

import java.util.List;

public class WinBotTest extends WinBot {

    public static void main(String[] args) {

        WinBot.startServer(WinBotTest.class, "127.0.0.1", 26678, "E:\\aibote\\Aibote\\");


    }

    @Override
    public void webMain() {
        this.sleep(5000);
        this.initOcr("127.0.0.1", 9528);
        Region region = new Region();
        region.left = 0;
        region.top = 216;
        region.right = 1327;
        region.bottom = 1137;
        List<OCRResult> list = this.ocr("5180720", region, 5, 0, 0, Mode.front);
        StringBuilder words = new StringBuilder();
        if (list != null) {
            list.forEach((obj) -> {
                words.append(obj.word).append("\n");
            });
            System.out.println(words);
        }
    }
}
