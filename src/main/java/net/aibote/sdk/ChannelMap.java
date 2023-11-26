package net.aibote.sdk;


import java.util.concurrent.ConcurrentHashMap;

public class ChannelMap implements Runnable {
    /**
     * 存放客户端标识ID（消息ID）与channel的对应关系
     */
    private static volatile ConcurrentHashMap<String, AiBot> concurrentHashMap = new ConcurrentHashMap<>();
    private static ChannelMap channelMap = new ChannelMap();

    private ChannelMap() {
    }

    public static ChannelMap instance() {
        return channelMap;
    }

    public void put(String key, AiBot aiBot) {
        concurrentHashMap.put(key, aiBot);
    }

    public AiBot get(String id) {
        return concurrentHashMap.get(id);
    }

    // 监控每个线程中的socket是否存活，异常socket将被移除
    @Override
    public void run() {
        while (true) {
            concurrentHashMap.forEachKey(1, (key) -> {
                AiBot aiBot = concurrentHashMap.get(key);
                if (aiBot.clientCocket == null || aiBot.clientCocket.isClosed()) {
                    concurrentHashMap.remove(aiBot.getKeyId()); // 如果连接不在了，则删除监控
                }
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }

}
