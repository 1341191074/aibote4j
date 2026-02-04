package net.aibote.examples;

import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;

public class ClientDriverStartupExample {
    public static void main(String[] args) {
//        String serverIp = "";
//        String serverPort = "0";
//        String browserName = "chrome";
//        String debugPort = "0"; // 可以手动指定
//        String userDataDir = null;
//        String browserPath = null;
//        String argument = null;
//        String webDriverPath = null;
//
//        startWebClientDriver(serverIp, serverPort, browserName, browserPath, debugPort, argument, userDataDir, webDriverPath);

        String winDriverPath = "E:\\Aibote\\";
        String winServerIp = "127.0.0.1";
        String winServerPort = "16999";
        startWinClientDriver(winDriverPath, winServerIp, winServerPort);
    }

    public static void startWebClientDriver(String serverIp, String serverPort, String browserName, String browserPath, String debugPort, String argument, String userDataDir, String webDriverPath) {
        JSONObject json = new JSONObject();
        json.put("serverIp", serverIp);
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
            //log.info(command);
            Process process = Runtime.getRuntime().exec(command);
            //log.info("启动driver");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startWinClientDriver(String driverPath, String serverIp, String serverPort) {
        try {
            String command = "WindowsDriver.exe";
            if (null != driverPath) {
                command = driverPath + command;
            }
            command += " " + serverIp + " " + serverPort;
            System.out.println(command);
            Process process = Runtime.getRuntime().exec(command);
            System.out.println("启动driver");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
