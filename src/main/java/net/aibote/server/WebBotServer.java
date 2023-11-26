package net.aibote.server;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class WebBotServer extends AiboteServer {

    //private static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(1, 50, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

    public void runLocalClient(int serverPort, String browserName, int debugPort, String userDataDir, String browserPath, String argument, String webDriverPath) {
        if (StringUtils.isBlank(browserName)) {
            browserName = "chrome";
        }
        if (debugPort <= 0) {
            debugPort = 9223;
        }

        JSONObject json = new JSONObject();
        json.put("serverIp", "127.0.0.1");
        json.put("serverPort", serverPort);
        json.put("browserName", browserName);
        json.put("browserPath", browserPath);
        json.put("debugPort", debugPort); //指定端口的情况下，必须手动打开浏览器
        json.put("userDataDir", userDataDir);
        json.put("argument", argument);
        String jsonStr = json.toJSONString().replace("\"", "\\\"");

        try {
            String command = "WebDriver.exe";
            if (null != webDriverPath) {
                command = webDriverPath + command;
            }
            command += "  \"" + jsonStr + "\" ";

            Process process = Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
