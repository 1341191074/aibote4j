package net.aibote.sdk.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求追踪处理器
 * 为每个请求分配唯一的correlation ID
 * 用于分布式追踪和日志关联
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class RequestTraceHandler {

    private static final RequestTraceHandler INSTANCE = new RequestTraceHandler();
    private static final ThreadLocal<String> CORRELATION_ID_HOLDER = new ThreadLocal<>();
    private static final AtomicLong REQUEST_COUNTER = new AtomicLong(0);
    private static final ConcurrentHashMap<String, RequestMetadata> REQUEST_METADATA = new ConcurrentHashMap<>();

    private RequestTraceHandler() {
    }

    /**
     * 获取单例实例
     * @return RequestTraceHandler单例
     */
    public static RequestTraceHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 生成新的correlation ID
     * @return correlation ID
     */
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成带有序列号的correlation ID
     * @return correlation ID with sequence
     */
    public String generateCorrelationIdWithSequence() {
        long sequence = REQUEST_COUNTER.incrementAndGet();
        String uuid = UUID.randomUUID().toString();
        return sequence + "-" + uuid;
    }

    /**
     * 设置当前线程的correlation ID
     * @param correlationId correlation ID
     */
    public void setCorrelationId(String correlationId) {
        CORRELATION_ID_HOLDER.set(correlationId);
    }

    /**
     * 获取当前线程的correlation ID
     * @return correlation ID，如果未设置则返回null
     */
    public String getCorrelationId() {
        return CORRELATION_ID_HOLDER.get();
    }

    /**
     * 获取或创建correlation ID
     * @return correlation ID
     */
    public String getOrCreateCorrelationId() {
        String correlationId = CORRELATION_ID_HOLDER.get();
        if (correlationId == null) {
            correlationId = generateCorrelationId();
            CORRELATION_ID_HOLDER.set(correlationId);
        }
        return correlationId;
    }

    /**
     * 清除当前线程的correlation ID
     */
    public void clearCorrelationId() {
        CORRELATION_ID_HOLDER.remove();
    }

    /**
     * 记录请求元数据
     * @param correlationId correlation ID
     * @param command 命令
     * @param botType 机器人类型
     */
    public void recordRequest(String correlationId, String command, String botType) {
        if (correlationId == null) {
            log.warn("Cannot record request with null correlation ID");
            return;
        }

        RequestMetadata metadata = new RequestMetadata(
                correlationId,
                command,
                botType,
                System.currentTimeMillis()
        );

        REQUEST_METADATA.put(correlationId, metadata);
        log.debug("Request recorded: {} for command: {}", correlationId, command);
    }

    /**
     * 获取请求元数据
     * @param correlationId correlation ID
     * @return 请求元数据
     */
    public RequestMetadata getRequestMetadata(String correlationId) {
        return REQUEST_METADATA.get(correlationId);
    }

    /**
     * 更新请求完成时间
     * @param correlationId correlation ID
     */
    public void markRequestComplete(String correlationId) {
        RequestMetadata metadata = REQUEST_METADATA.get(correlationId);
        if (metadata != null) {
            metadata.setCompletedTime(System.currentTimeMillis());
            log.debug("Request completed: {} duration: {}ms",
                    correlationId,
                    metadata.getDurationMillis());
        }
    }

    /**
     * 移除请求元数据
     * @param correlationId correlation ID
     */
    public void removeRequestMetadata(String correlationId) {
        REQUEST_METADATA.remove(correlationId);
    }

    /**
     * 获取所有正在进行的请求
     * @return 请求元数据映射
     */
    public ConcurrentHashMap<String, RequestMetadata> getAllRequestMetadata() {
        return new ConcurrentHashMap<>(REQUEST_METADATA);
    }

    /**
     * 获取总请求数
     * @return 总请求数
     */
    public long getTotalRequestCount() {
        return REQUEST_COUNTER.get();
    }

    /**
     * 清除所有请求元数据
     */
    public void clearAllMetadata() {
        REQUEST_METADATA.clear();
        log.info("All request metadata cleared");
    }

    /**
     * 请求元数据内部类
     */
    public static class RequestMetadata {
        private final String correlationId;
        private final String command;
        private final String botType;
        private final long startTime;
        private volatile long completedTime;

        public RequestMetadata(String correlationId, String command, String botType, long startTime) {
            this.correlationId = correlationId;
            this.command = command;
            this.botType = botType;
            this.startTime = startTime;
            this.completedTime = 0;
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public String getCommand() {
            return command;
        }

        public String getBotType() {
            return botType;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getCompletedTime() {
            return completedTime;
        }

        public void setCompletedTime(long completedTime) {
            this.completedTime = completedTime;
        }

        public long getDurationMillis() {
            if (completedTime == 0) {
                return System.currentTimeMillis() - startTime;
            }
            return completedTime - startTime;
        }

        public boolean isCompleted() {
            return completedTime > 0;
        }
    }
}

