package net.aibote.utils.config;

import lombok.Data;

/**
 * 机器人配置类
 * 集中管理所有配置参数
 */
@Data
public class BotConfig {
    
    /**
     * 通信相关配置
     */
    private CommunicationConfig communication = new CommunicationConfig();
    
    /**
     * 性能相关配置
     */
    private PerformanceConfig performance = new PerformanceConfig();
    
    /**
     * 日志相关配置
     */
    private LoggingConfig logging = new LoggingConfig();
    
    /**
     * 安全相关配置
     */
    private SecurityConfig security = new SecurityConfig();
    
    @Data
    public static class CommunicationConfig {
        // 默认响应超时时间（毫秒）
        private long responseTimeout = 2000L;
        // 延迟响应超时时间（毫秒）
        private long delayResponseTimeout = 6000L;
        // 重试次数
        private int retryTimes = 3;
        // 重试间隔（毫秒）
        private int retryInterval = 500;
        // 连接池大小
        private int connectionPoolSize = 10;
    }
    
    @Data
    public static class PerformanceConfig {
        // 最大并发数
        private int maxConcurrency = 100;
        // 线程池大小
        private int threadPoolSize = 10;
        // 缓冲区大小
        private int bufferSize = 8192;
        // 内存限制（MB）
        private int memoryLimit = 512;
    }
    
    @Data
    public static class LoggingConfig {
        // 日志级别
        private String level = "INFO";
        // 日志文件大小限制（MB）
        private int maxFileSize = 10;
        // 保留天数
        private int retentionDays = 7;
        // 是否启用详细日志
        private boolean detailedLogEnabled = false;
    }
    
    @Data
    public static class SecurityConfig {
        // 是否启用认证
        private boolean authEnabled = false;
        // 认证令牌有效期（分钟）
        private int tokenExpirationMinutes = 60;
        // 最大连接数限制
        private int maxConnections = 1000;
        // IP白名单
        private String[] ipWhitelist = {};
    }
}