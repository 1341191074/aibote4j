package net.aibote.security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token管理器 - 负责生成、验证和管理认证令牌
 *
 * 特点:
 * - 线程安全的Token存储
 * - 自动过期处理
 * - 支持Token撤销
 * - 加强的安全性
 */
@Slf4j
public class TokenManager {

    private static volatile TokenManager instance;
    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();
    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final AtomicInteger tokenCount = new AtomicInteger(0);

    private static final long DEFAULT_EXPIRATION = 3600000; // 1小时
    private static final int TOKEN_LENGTH = 32;

    /**
     * Token信息类
     */
    @Data
    public static class TokenInfo {
        private String token;
        private String userId;
        private String clientId;
        private long createdAt;
        private long expiresAt;
        private boolean valid;
        private int accessCount;
        private long lastAccessTime;
        private Set<String> permissions = new HashSet<>();

        /**
         * 检查Token是否已过期
         */
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }

        /**
         * 检查Token是否仍然有效
         */
        public boolean isValid() {
            return valid && !isExpired();
        }
    }

    private TokenManager() {
        // 启动清理线程
        startCleanupTask();
    }

    /**
     * 获取单例实例
     */
    public static TokenManager getInstance() {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) {
                    instance = new TokenManager();
                }
            }
        }
        return instance;
    }

    /**
     * 生成新Token
     */
    public String generateToken(String userId, String clientId, Set<String> permissions) {
        String token = generateSecureToken();

        TokenInfo info = new TokenInfo();
        info.setToken(token);
        info.setUserId(userId);
        info.setClientId(clientId);
        info.setCreatedAt(System.currentTimeMillis());
        info.setExpiresAt(info.getCreatedAt() + DEFAULT_EXPIRATION);
        info.setValid(true);
        info.setAccessCount(0);
        info.setLastAccessTime(info.getCreatedAt());
        info.setPermissions(permissions != null ? new HashSet<>(permissions) : new HashSet<>());

        tokens.put(token, info);
        tokenCount.incrementAndGet();

        log.info("生成新Token - userId: {}, clientId: {}, tokenCount: {}",
                userId, clientId, tokenCount.get());

        return token;
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // 检查是否被撤销
        if (revokedTokens.containsKey(token)) {
            return false;
        }

        TokenInfo info = tokens.get(token);
        if (info == null) {
            return false;
        }

        if (!info.isValid()) {
            return false;
        }

        // 更新最后访问时间
        info.setLastAccessTime(System.currentTimeMillis());
        info.setAccessCount(info.getAccessCount() + 1);

        return true;
    }

    /**
     * 获取Token信息
     */
    public TokenInfo getTokenInfo(String token) {
        return validateToken(token) ? tokens.get(token) : null;
    }

    /**
     * 检查权限
     */
    public boolean hasPermission(String token, String permission) {
        TokenInfo info = getTokenInfo(token);
        return info != null && info.getPermissions().contains(permission);
    }

    /**
     * 撤销Token
     */
    public void revokeToken(String token) {
        TokenInfo info = tokens.get(token);
        if (info != null) {
            info.setValid(false);
            revokedTokens.put(token, System.currentTimeMillis());
            log.info("Token已撤销 - userId: {}", info.getUserId());
        }
    }

    /**
     * 撤销特定用户的所有Token
     */
    public void revokeUserTokens(String userId) {
        tokens.values().stream()
                .filter(t -> t.getUserId().equals(userId))
                .forEach(t -> {
                    t.setValid(false);
                    revokedTokens.put(t.getToken(), System.currentTimeMillis());
                });
        log.info("已撤销用户{}的所有Token", userId);
    }

    /**
     * 生成安全的Token
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    /**
     * 启动后台清理任务 - 定期清理过期Token
     */
    private void startCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // 每分钟检查一次
                    cleanupExpiredTokens();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setName("TokenCleanupThread");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    /**
     * 清理过期的Token
     */
    private void cleanupExpiredTokens() {
        int initialSize = tokens.size();

        tokens.entrySet().removeIf(entry -> {
            TokenInfo info = entry.getValue();
            return info.isExpired() || !info.isValid();
        });

        // 清理撤销的Token（保留24小时）
        long cutoffTime = System.currentTimeMillis() - 86400000;
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() < cutoffTime);

        int finalSize = tokens.size();
        if (initialSize != finalSize) {
            log.debug("Token清理 - 删除: {}, 当前数量: {}", initialSize - finalSize, finalSize);
        }
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTokens", tokenCount.get());
        stats.put("activeTokens", tokens.size());
        stats.put("revokedTokens", revokedTokens.size());
        return stats;
    }
}

