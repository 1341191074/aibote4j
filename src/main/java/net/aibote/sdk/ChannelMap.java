package net.aibote.sdk;


import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelMap implements Runnable {
    /**
     * 存放客户端标识ID（消息ID）与channel的对应关系
     */
    @Getter
    private static volatile ConcurrentHashMap<String, AiBot> channelMap = new ConcurrentHashMap<>();

    ChannelMap() {
    }

    public static AiBot get(String id) {
        return getChannelMap().get(id);
    }

    // 监控每个线程中的socket是否存活，异常socket将被移除
    @Override
    public void run() {
        while (true) {
            channelMap.forEachKey(1, (key) -> {
                AiBot aiBot = channelMap.get(key);
                if (aiBot.clientCocket.isClosed()) {
                    channelMap.remove(aiBot.getKeyId()); // 如果连接不在了，则删除监控
                }
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }

}
