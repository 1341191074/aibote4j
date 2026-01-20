package net.aibote.utils.config;

import lombok.extern.slf4j.Slf4j;
import net.aibote.utils.YamlUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 配置管理器
 * 负责加载和管理全局配置
 * 支持从环境变量覆盖配置值，提供线程安全的配置访问
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class ConfigManager {
    
    private static final Object LOCK = new Object();
    private static volatile ConfigManager instance;
    private static final String CONFIG_FILE_NAME = "bot-config.yml";
    private static final String CONFIG_ENV_VAR = "AIBOTE_CONFIG";

    private volatile BotConfig config;

    private ConfigManager() {
        loadConfig();
    }
    
    /**
     * 获取单例实例
     * @return ConfigManager单例
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 加载配置
     * 优先级：环境变量 > classpath > 文件系统 > 默认配置
     */
    private void loadConfig() {
        try {
            // 1. 检查环境变量中的配置路径
            String envConfigPath = System.getenv(CONFIG_ENV_VAR);
            if (envConfigPath != null && !envConfigPath.isEmpty()) {
                if (Files.exists(Paths.get(envConfigPath))) {
                    config = YamlUtils.loadAsFile(envConfigPath, BotConfig.class);
                    log.info("从环境变量指定路径加载配置文件成功: {}", envConfigPath);
                    applyEnvironmentOverrides(config);
                    return;
                } else {
                    log.warn("环境变量 {} 指定的配置文件不存在: {}", CONFIG_ENV_VAR, envConfigPath);
                }
            }

            // 2. 尝试从classpath加载配置文件
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
            if (inputStream != null) {
                config = YamlUtils.loadAs(inputStream, BotConfig.class);
                log.info("从classpath加载配置文件成功");
                applyEnvironmentOverrides(config);
                return;
            }

            // 3. 尝试从文件系统加载
            if (Files.exists(Paths.get(CONFIG_FILE_NAME))) {
                config = YamlUtils.loadAsFile(CONFIG_FILE_NAME, BotConfig.class);
                log.info("从文件系统加载配置文件成功");
                applyEnvironmentOverrides(config);
                return;
            }

            // 4. 使用默认配置
            config = new BotConfig();
            log.warn("未找到配置文件，使用默认配置");
            applyEnvironmentOverrides(config);

        } catch (Exception e) {
            log.error("加载配置文件失败，使用默认配置", e);
            config = new BotConfig();
        }
    }
    
    /**
     * 应用环境变量覆盖配置
     * @param config 配置对象
     */
    private void applyEnvironmentOverrides(BotConfig config) {
        // 允许通过环境变量覆盖特定的配置值
        String responseTimeout = System.getenv("AIBOTE_RESPONSE_TIMEOUT");
        if (responseTimeout != null && !responseTimeout.isEmpty()) {
            try {
                config.getCommunication().setResponseTimeout(Long.parseLong(responseTimeout));
                log.info("从环境变量覆盖响应超时: {}", responseTimeout);
            } catch (NumberFormatException e) {
                log.warn("无效的响应超时值: {}", responseTimeout);
            }
        }

        String maxConcurrency = System.getenv("AIBOTE_MAX_CONCURRENCY");
        if (maxConcurrency != null && !maxConcurrency.isEmpty()) {
            try {
                config.getPerformance().setMaxConcurrency(Integer.parseInt(maxConcurrency));
                log.info("从环境变量覆盖最大并发数: {}", maxConcurrency);
            } catch (NumberFormatException e) {
                log.warn("无效的最大并发数值: {}", maxConcurrency);
            }
        }
    }

    /**
     * 重新加载配置
     * 使用同步锁确保线程安全
     */
    public void reloadConfig() {
        synchronized (LOCK) {
            loadConfig();
            log.info("配置重新加载完成");
        }
    }
    
    /**
     * 获取配置
     * @return 机器人配置
     */
    public BotConfig getConfig() {
        return config;
    }
    
    /**
     * 获取通信配置
     * @return 通信配置
     */
    public BotConfig.CommunicationConfig getCommunicationConfig() {
        return config.getCommunication();
    }
    
    /**
     * 获取性能配置
     * @return 性能配置
     */
    public BotConfig.PerformanceConfig getPerformanceConfig() {
        return config.getPerformance();
    }
    
    /**
     * 获取日志配置
     * @return 日志配置
     */
    public BotConfig.LoggingConfig getLoggingConfig() {
        return config.getLogging();
    }
    
    /**
     * 获取安全配置
     * @return 安全配置
     */
    public BotConfig.SecurityConfig getSecurityConfig() {
        return config.getSecurity();
    }
}