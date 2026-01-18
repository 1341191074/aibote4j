package net.aibote.utils.config;

import lombok.extern.slf4j.Slf4j;
import net.aibote.utils.YamlUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 配置管理器
 * 负责加载和管理全局配置
 */
@Slf4j
public class ConfigManager {
    
    private static volatile ConfigManager instance;
    private BotConfig config;
    
    private ConfigManager() {
        loadConfig();
    }
    
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 加载配置
     */
    private void loadConfig() {
        try {
            // 尝试从classpath加载配置文件
            String configFile = "bot-config.yml";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFile);
            
            if (inputStream != null) {
                config = YamlUtils.loadAs(inputStream, BotConfig.class);
                log.info("从classpath加载配置文件成功");
            } else {
                // 尝试从文件系统加载
                if (Files.exists(Paths.get(configFile))) {
                    config = YamlUtils.loadAsFile(configFile, BotConfig.class);
                    log.info("从文件系统加载配置文件成功");
                } else {
                    // 使用默认配置
                    config = new BotConfig();
                    log.warn("未找到配置文件，使用默认配置");
                }
            }
        } catch (Exception e) {
            log.error("加载配置文件失败，使用默认配置", e);
            config = new BotConfig();
        }
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfig();
        log.info("配置重新加载完成");
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