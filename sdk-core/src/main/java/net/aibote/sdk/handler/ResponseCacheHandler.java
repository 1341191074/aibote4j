package net.aibote.sdk.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 响应缓存处理器 (基于内存)
 * 用于缓存机器人命令的响应结果
 * 支持TTL过期机制，防止内存泄漏
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class ResponseCacheHandler {

    private static final ResponseCacheHandler INSTANCE = new ResponseCacheHandler();
    private static final long DEFAULT_TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private static final long CLEANUP_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(1);

    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    private volatile long lastCleanupTime = System.currentTimeMillis();

    private ResponseCacheHandler() {
        startCleanupThread();
    }

    /**
     * 获取单例实例
     * @return ResponseCacheHandler单例
     */
    public static ResponseCacheHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 缓存响应结果
     * @param key 缓存键
     * @param response 响应结果
     */
    public void cache(String key, byte[] response) {
        if (key == null || response == null) {
            log.warn("Cannot cache null key or response");
            return;
        }

        CachedResponse cachedResponse = new CachedResponse(response, System.currentTimeMillis());
        cache.put(key, cachedResponse);
        log.debug("Response cached for key: {}", key);
    }

    /**
     * 缓存响应结果（自定义TTL）
     * @param key 缓存键
     * @param response 响应结果
     * @param ttlMillis TTL毫秒数
     */
    public void cache(String key, byte[] response, long ttlMillis) {
        if (key == null || response == null) {
            log.warn("Cannot cache null key or response");
            return;
        }

        CachedResponse cachedResponse = new CachedResponse(response, System.currentTimeMillis(), ttlMillis);
        cache.put(key, cachedResponse);
        log.debug("Response cached for key: {} with TTL: {}ms", key, ttlMillis);
    }

    /**
     * 获取缓存的响应
     * @param key 缓存键
     * @return 缓存的响应，如果不存在或已过期返回null
     */
    public byte[] get(String key) {
        if (key == null) {
            return null;
        }

        CachedResponse cachedResponse = cache.get(key);
        if (cachedResponse != null && !cachedResponse.isExpired()) {
            log.debug("Response found in cache for key: {}", key);
            return cachedResponse.getResponse();
        }

        // 移除过期的缓存
        if (cachedResponse != null && cachedResponse.isExpired()) {
            cache.remove(key);
            log.debug("Expired cache removed for key: {}", key);
        }

        return null;
    }

    /**
     * 移除缓存
     * @param key 缓存键
     */
    public void remove(String key) {
        if (key != null) {
            cache.remove(key);
            log.debug("Cache removed for key: {}", key);
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        cache.clear();
        log.info("All cache cleared");
    }

    /**
     * 获取缓存大小
     * @return 缓存项数量
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * 启动清理线程
     */
    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(CLEANUP_INTERVAL_MILLIS);
                    performCleanup();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Cleanup thread interrupted");
                    break;
                }
            }
        }, "ResponseCacheCleanup");

        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    /**
     * 执行缓存清理
     */
    private void performCleanup() {
        long now = System.currentTimeMillis();

        // 检查是否需要清理
        if (now - lastCleanupTime < CLEANUP_INTERVAL_MILLIS) {
            return;
        }

        lastCleanupTime = now;

        int initialSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int finalSize = cache.size();

        if (initialSize > finalSize) {
            log.debug("Cache cleanup: removed {} expired entries", initialSize - finalSize);
        }
    }

    /**
     * 缓存的响应内部类
     */
    private static class CachedResponse {
        private final byte[] response;
        private final long cachedTime;
        private final long ttlMillis;

        public CachedResponse(byte[] response, long cachedTime) {
            this(response, cachedTime, DEFAULT_TTL_MILLIS);
        }

        public CachedResponse(byte[] response, long cachedTime, long ttlMillis) {
            this.response = response;
            this.cachedTime = cachedTime;
            this.ttlMillis = ttlMillis;
        }

        public byte[] getResponse() {
            return response;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - cachedTime > ttlMillis;
        }
    }
}

