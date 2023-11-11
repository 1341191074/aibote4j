package net.aibote.sdk;


import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelMap {
    /**
     * 存放客户端标识ID（消息ID）与channel的对应关系
     */
    private static volatile ConcurrentHashMap<String, Socket> channelMap = null;

    private ChannelMap() {
    }

    public static ConcurrentHashMap<String, Socket> getChannelMap() {
        if (null == channelMap) {
            synchronized (ChannelMap.class) {
                if (null == channelMap) {
                    channelMap = new ConcurrentHashMap<>();
                }
            }
        }
        return channelMap;
    }

    public static Socket getChannel(String id) {
        return getChannelMap().get(id);
    }
}
