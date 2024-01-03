package net.aibote.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class ClientManager {
    private Map<String, AiboteChannel> clients = new HashMap<>();
    private static final ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(200);

    public synchronized static String poll() {
        return arrayBlockingQueue.poll();
    }

    public synchronized static void offer(String keyId) {
        arrayBlockingQueue.offer(keyId);
    }

    public void add(String keyId, AiboteChannel ctc) {
        this.clients.put(keyId, ctc);
        ClientManager.offer(keyId);
    }

    public void remove(String keyId) {
        this.clients.remove(keyId);
    }

    public AiboteChannel get(String channelId) {
        return clients.get(channelId);
    }
}
