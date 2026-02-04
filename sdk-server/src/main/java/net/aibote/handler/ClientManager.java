package net.aibote.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 客户端管理器
 * 负责管理所有客户端连接和分发客户端请求
 * 使用并发集合确保线程安全
 */
@Slf4j
public class ClientManager {

    private static final int DEFAULT_QUEUE_CAPACITY = 200;
    private static final ClientManager INSTANCE = new ClientManager();
    
    // 添加定时清理服务
    private final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "ClientManager-Cleanup");
        t.setDaemon(true);
        return t;
    });

    private final Map<String, AiboteChannel> clients = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String> clientQueue = new LinkedBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);
    private final AtomicInteger clientCount = new AtomicInteger(0);

    private ClientManager() {
        // 启动定期清理任务
        startPeriodicCleanup();
    }
    
    /**
     * 启动定期清理任务
     */
    private void startPeriodicCleanup() {
        cleanupScheduler.scheduleWithFixedDelay(this::cleanupInactiveClients, 
            30, 30, TimeUnit.SECONDS);
    }

    /**
     * 获取单例实例
     * @return ClientManager单例
     */
    public static ClientManager getInstance() {
        return INSTANCE;
    }

    /**
     * 获取可用的客户端ID
     * @return 客户端ID，如果队列为空则返回null
     */
    public String poll() {
        return clientQueue.poll();
    }

    /**
     * 尝试获取可用的客户端ID，支持超时
     * @param timeoutMillis 超时时间（毫秒）
     * @return 客户端ID，如果超时则返回null
     * @throws InterruptedException 如果线程被中断
     */
    public String pollWithTimeout(long timeoutMillis) throws InterruptedException {
        return clientQueue.poll(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 将客户端ID加入可用队列
     * @param keyId 客户端ID
     * @return 如果添加成功返回true，队列满则返回false
     */
    public boolean offer(String keyId) {
        return clientQueue.offer(keyId);
    }

    /**
     * 添加新的客户端连接
     * @param keyId 客户端ID
     * @param channel 客户端通道
     */
    public void add(String keyId, AiboteChannel channel) {
        if (keyId == null || channel == null) {
            log.warn("Cannot add null client or channel");
            return;
        }
        clients.put(keyId, channel);
        offer(keyId);
        clientCount.incrementAndGet();
        log.debug("Client added: {}, total clients: {}", keyId, clientCount.get());
    }

    /**
     * 移除客户端连接
     * @param keyId 客户端ID
     */
    public void remove(String keyId) {
        AiboteChannel removedChannel = clients.remove(keyId);
        if (removedChannel != null) {
            clientCount.decrementAndGet();
            // 清理通道资源
            try {
                if (removedChannel.aiboteChanel != null && 
                    removedChannel.aiboteChanel.channel().isActive()) {
                    removedChannel.aiboteChanel.close();
                }
            } catch (Exception e) {
                log.debug("关闭客户端通道时出现异常: {}", keyId, e);
            }
            log.debug("Client removed: {}, remaining clients: {}", keyId, clientCount.get());
        }
    }
    
    /**
     * 清理不活跃的客户端连接
     */
    private void cleanupInactiveClients() {
        try {
            int cleanedCount = 0;
            for (Map.Entry<String, AiboteChannel> entry : clients.entrySet()) {
                AiboteChannel channel = entry.getValue();
                if (channel.aiboteChanel != null && 
                    !channel.aiboteChanel.channel().isActive()) {
                    remove(entry.getKey());
                    cleanedCount++;
                }
            }
            if (cleanedCount > 0) {
                log.info("清理了 {} 个不活跃的客户端连接", cleanedCount);
            }
        } catch (Exception e) {
            log.warn("执行定期清理任务时出现异常", e);
        }
    }
    
    /**
     * 关闭客户端管理器，释放所有资源
     */
    public void shutdown() {
        try {
            cleanupScheduler.shutdown();
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cleanupScheduler.shutdownNow();
        }
        
        // 清理所有客户端连接
        clear();
        log.info("ClientManager 已关闭");
    }

    /**
     * 获取客户端连接
     * @param channelId 客户端ID
     * @return 客户端通道，如果不存在则返回null
     */
    public AiboteChannel get(String channelId) {
        return clients.get(channelId);
    }

    /**
     * 获取当前连接的客户端数量
     * @return 客户端数量
     */
    public int getClientCount() {
        return clientCount.get();
    }

    /**
     * 获取队列中可用的客户端数量
     * @return 可用客户端数量
     */
    public int getAvailableClientCount() {
        return clientQueue.size();
    }

    /**
     * 清空所有客户端连接
     */
    public void clear() {
        clients.clear();
        clientQueue.clear();
        clientCount.set(0);
        log.info("All clients cleared");
    }
}
