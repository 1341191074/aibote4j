package net.aibote.sdk.pool;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import net.aibote.utils.config.ConfigManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接池管理器
 * 实现连接池模式，用于管理多个客户端连接
 */
@Slf4j
public class ConnectionPoolManager {
    
    private static volatile ConnectionPoolManager instance;
    private final ConcurrentHashMap<String, ChannelHandlerContext> connections = new ConcurrentHashMap<>();
    private final AtomicInteger connectionCounter = new AtomicInteger(0);
    
    // 生产级特性
    private final ScheduledExecutorService monitorExecutor = new ScheduledThreadPoolExecutor(1, r -> {
        Thread t = new Thread(r, "ConnectionPoolMonitor");
        t.setDaemon(true);
        return t;
    });
    
    private ConnectionPoolManager() {
        // 启动监控任务
        startMonitoring();
    }
    
    public static ConnectionPoolManager getInstance() {
        if (instance == null) {
            synchronized (ConnectionPoolManager.class) {
                if (instance == null) {
                    instance = new ConnectionPoolManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 添加连接
     * @param channelId 连接ID
     * @param ctx 通道上下文
     */
    public void addConnection(String channelId, ChannelHandlerContext ctx) {
        if (connections.putIfAbsent(channelId, ctx) == null) {
            connectionCounter.incrementAndGet();
            log.info("添加连接: {}, 当前连接总数: {}", channelId, connectionCounter.get());
        } else {
            log.warn("连接ID {} 已存在，无法重复添加", channelId);
        }
    }
    
    /**
     * 获取连接
     * @param channelId 连接ID
     * @return 通道上下文
     */
    public ChannelHandlerContext getConnection(String channelId) {
        return connections.get(channelId);
    }
    
    /**
     * 移除连接
     * @param channelId 连接ID
     */
    public void removeConnection(String channelId) {
        ChannelHandlerContext ctx = connections.remove(channelId);
        if (ctx != null) {
            connectionCounter.decrementAndGet();
            log.info("移除连接: {}, 当前连接总数: {}", channelId, connectionCounter.get());
        }
    }
    
    /**
     * 获取连接总数
     * @return 连接总数
     */
    public int getConnectionCount() {
        return connectionCounter.get();
    }
    
    /**
     * 关闭所有连接
     */
    public void closeAllConnections() {
        connections.values().forEach(ctx -> {
            if (ctx != null && ctx.channel().isOpen()) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    log.error("关闭连接时发生错误", e);
                }
            }
        });
        connections.clear();
        connectionCounter.set(0);
        log.info("已关闭所有连接，当前连接数: 0");
    }
    
    /**
     * 启动监控任务
     */
    private void startMonitoring() {
        // 定期检查连接健康状况
        monitorExecutor.scheduleAtFixedRate(this::healthCheck, 30, 30, TimeUnit.SECONDS);
        
        // JVM关闭时清理资源
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }
    
    /**
     * 健康检查
     */
    private void healthCheck() {
        log.debug("执行连接池健康检查，当前连接数: {}", connections.size());
        
        // 检查并移除失效连接
        connections.entrySet().removeIf(entry -> {
            ChannelHandlerContext ctx = entry.getValue();
            if (ctx == null || !ctx.channel().isActive() || !ctx.channel().isOpen()) {
                log.warn("检测到失效连接: {}，正在移除", entry.getKey());
                connectionCounter.decrementAndGet();
                return true;
            }
            return false;
        });
        
        // 检查连接数是否超过限制
        int maxConnections = ConfigManager.getInstance().getSecurityConfig().getMaxConnections();
        if (connectionCounter.get() > maxConnections) {
            log.warn("当前连接数({})超过限制({})", connectionCounter.get(), maxConnections);
        }
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        log.info("正在清理连接池资源...");
        closeAllConnections();
        monitorExecutor.shutdown();
        try {
            if (!monitorExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                monitorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitorExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("连接池资源清理完成");
    }
}