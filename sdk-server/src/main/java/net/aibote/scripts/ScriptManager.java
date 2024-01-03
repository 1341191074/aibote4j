package net.aibote.scripts;

import net.aibote.handler.AiboteChannel;
import net.aibote.handler.ClientManager;
import net.aibote.handler.impl.AndroidClientManager;

public class ScriptManager implements Runnable {

    @Override
    public void run() {
        while (true) {
            String channelId = ClientManager.poll();
            if (null != channelId) {
                Thread.ofVirtual().start(() -> {
                    AiboteChannel aiboteChannel = AndroidClientManager.getInstance().get(channelId);
                    AndroidBotTest androidBotTest = new AndroidBotTest();
                    aiboteChannel.setAibote(androidBotTest);
                    androidBotTest.doScript();
                });
            }else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
