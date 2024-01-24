package net.aibote;

import net.aibote.scripts.ScriptManager;
import net.aibote.server.impl.AndroidServer;
import net.aibote.server.impl.WebServer;
import net.aibote.server.impl.WinServer;

public class App {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            AndroidServer.getInstance().start();
        });
        t1.start();

        /*Thread t2 = new Thread(() -> {
            WebServer.getInstance().start();
        });
        t2.start();

        Thread t3 = new Thread(() -> {
            WinServer.getInstance().start();
        });
        t3.start();*/

        Thread scriptManager = Thread.ofVirtual().unstarted(new ScriptManager());
        scriptManager.start();
    }
}
