package net.aibote.sdk.monitor;

import net.aibote.sdk.pool.ConnectionPoolManager;
import net.aibote.utils.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器人健康指标监控
 * 提供系统健康状态检查功能
 */
public class BotHealthIndicator {
    
    private static volatile BotHealthIndicator instance;
    
    private BotHealthIndicator() {}
    
    public static BotHealthIndicator getInstance() {
        if (instance == null) {
            synchronized (BotHealthIndicator.class) {
                if (instance == null) {
                    instance = new BotHealthIndicator();
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取系统健康状态
     * @return 健康状态信息
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> healthInfo = new HashMap<>();
        
        // 连接池状态
        healthInfo.put("connectionPool", getConnectionPoolStatus());
        
        // 配置状态
        healthInfo.put("config", getConfigStatus());
        
        // 系统资源状态
        healthInfo.put("systemResources", getSystemResourcesStatus());
        
        // 健康状态汇总
        healthInfo.put("overallStatus", isHealthy() ? "UP" : "DOWN");
        
        return healthInfo;
    }
    
    /**
     * 获取连接池状态
     * @return 连接池状态信息
     */
    private Map<String, Object> getConnectionPoolStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("activeConnections", ConnectionPoolManager.getInstance().getConnectionCount());
        status.put("maxAllowedConnections", ConfigManager.getInstance().getSecurityConfig().getMaxConnections());
        status.put("healthy", ConnectionPoolManager.getInstance().getConnectionCount() <= ConfigManager.getInstance().getSecurityConfig().getMaxConnections());
        return status;
    }
    
    /**
     * 获取配置状态
     * @return 配置状态信息
     */
    private Map<String, Object> getConfigStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("responseTimeout", ConfigManager.getInstance().getCommunicationConfig().getResponseTimeout());
        status.put("delayResponseTimeout", ConfigManager.getInstance().getCommunicationConfig().getDelayResponseTimeout());
        status.put("retryTimes", ConfigManager.getInstance().getCommunicationConfig().getRetryTimes());
        status.put("threadPoolSize", ConfigManager.getInstance().getPerformanceConfig().getThreadPoolSize());
        return status;
    }
    
    /**
     * 获取系统资源状态
     * @return 系统资源状态信息
     */
    private Map<String, Object> getSystemResourcesStatus() {
        Map<String, Object> status = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        status.put("maxMemoryMB", maxMemory / 1024 / 1024);
        status.put("totalMemoryMB", totalMemory / 1024 / 1024);
        status.put("usedMemoryMB", usedMemory / 1024 / 1024);
        status.put("freeMemoryMB", freeMemory / 1024 / 1024);
        status.put("memoryUsagePercentage", (int) ((double) usedMemory / maxMemory * 100));
        status.put("availableProcessors", runtime.availableProcessors());
        
        return status;
    }
    
    /**
     * 检查系统是否健康
     * @return 系统健康状态
     */
    public boolean isHealthy() {
        // 检查连接数是否超过限制
        int activeConnections = ConnectionPoolManager.getInstance().getConnectionCount();
        int maxConnections = ConfigManager.getInstance().getSecurityConfig().getMaxConnections();
        
        if (activeConnections > maxConnections) {
            return false;
        }
        
        // 检查内存使用率
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsageRatio = (double) usedMemory / maxMemory;
        
        // 如果内存使用超过90%，认为系统不健康
        return memoryUsageRatio < 0.9;
    }
}