package net.aibote.server;

import java.io.IOException;

public class WinBoteServer extends AiboteServer {

    public void runLocalClient(int serverPort, String driverPath) {
        try {
            String command = "WindowsDriver.exe";
            if (null != driverPath) {
                command = driverPath + command;
            }
            command += " 127.0.0.1 " + serverPort;
            Process process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
