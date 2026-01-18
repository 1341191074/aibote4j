package net.aibote.sdk.pool;

import net.aibote.sdk.AiBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端连接池管理器，用于管理Bot连接的生命周期
 */
public class ClientConnectionPool {
    
    private static final Logger log = LoggerFactory.getLogger(ClientConnectionPool.class);
    
    // 线程池执行器
    private final ThreadPoolExecutor executor;
    
    // 连接计数器
    private final AtomicInteger connectionCount = new AtomicInteger(0);
    
    // 最大连接数
    private final int maxConnections;
    
    // 单例实例
    private static volatile ClientConnectionPool instance;
    
    private ClientConnectionPool(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        this.maxConnections = maximumPoolSize;
        
        this.executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), // 限制队列大小，防止内存溢出
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "AiBot-Connection-" + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        t.setPriority(Thread.NORM_PRIORITY); // 设置普通优先级
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 如果队列满了，调用者自己执行任务
        );
    }
    
    /**
     * 获取单例实例
     * @return ClientConnectionPool实例
     */
    public static ClientConnectionPool getInstance() {
        if (instance == null) {
            synchronized (ClientConnectionPool.class) {
                if (instance == null) {
                    instance = new ClientConnectionPool(
                            Math.max(2, Runtime.getRuntime().availableProcessors()), // 核心线程数至少为2
                            200, // 最大线程数
                            60L // 保持活跃时间
                    );
                }
            }
        }
        return instance;
    }
    
    /**
     * 提交Bot任务
     * @param aiBot Bot实例
     * @return Future对象，可用于检查任务状态
     */
    public Future<?> submitBotTask(AiBot aiBot) {
        if (connectionCount.get() >= maxConnections) {
            log.warn("达到最大连接数限制: {}", maxConnections);
            throw new RejectedExecutionException("连接数已达上限: " + maxConnections);
        }
        
        connectionCount.incrementAndGet();
        return executor.submit(() -> {
            try {
                aiBot.run();
            } finally {
                connectionCount.decrementAndGet();
            }
        });
    }
    
    /**
     * 提交Bot任务并返回Future
     * @param task Bot实例
     * @return Future对象，可用于检查任务状态和结果
     */
    public <T> Future<T> submitBotTask(Callable<T> task) {
        if (connectionCount.get() >= maxConnections) {
            log.warn("达到最大连接数限制: {}", maxConnections);
            throw new RejectedExecutionException("连接数已达上限: " + maxConnections);
        }
        
        connectionCount.incrementAndGet();
        Future<T> future = executor.submit(() -> {
            try {
                return task.call();
            } finally {
                connectionCount.decrementAndGet();
            }
        });
        
        return future;
    }
    
    /**
     * 获取当前活跃连接数
     * @return 活跃连接数
     */
    public int getActiveConnections() {
        return connectionCount.get();
    }
    
    /**
     * 获取线程池信息
     * @return 线程池信息字符串
     */
    public String getPoolInfo() {
        return String.format(
                "Pool Info: Active=%d, Core=%d, Max=%d, QueueSize=%d, Completed=%d",
                executor.getActiveCount(),
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount()
        );
    }
    
    /**
     * 获取当前排队的任务数
     * @return 排队任务数
     */
    public int getQueuedTaskCount() {
        return executor.getQueue().size();
    }
    
    /**
     * 获取已完成的任务数
     * @return 已完成任务数
     */
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }
    
    /**
     * 获取总任务数（包括已完成和正在进行的）
     * @return 总任务数
     */
    public long getTotalTaskCount() {
        return executor.getTaskCount();
    }
    
    /**
     * 关闭连接池
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("线程池未在60秒内完全关闭，强制关闭");
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("线程池强制关闭失败");
                }
            }
        } catch (InterruptedException e) {
            log.warn("关闭线程池时被中断，强制关闭");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 立即关闭连接池
     */
    public void shutdownNow() {
        executor.shutdownNow();
    }
    
    /**
     * 检查连接池是否已关闭
     * @return 如果已关闭返回true，否则返回false
     */
    public boolean isShutdown() {
        return executor.isShutdown();
    }
    
    /**
     * 检查连接池是否已终止
     * @return 如果已终止返回true，否则返回false
     */
    public boolean isTerminated() {
        return executor.isTerminated();
    }
}