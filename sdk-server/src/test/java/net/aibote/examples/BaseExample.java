package net.aibote.examples;

import lombok.extern.slf4j.Slf4j;
import net.aibote.sdk.AbstractPlatformBot;

/**
 * 示例基类
 * 所有示例应继承此类，确保标准的初始化和清理流程
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public abstract class BaseExample {

    protected AbstractPlatformBot bot;
    protected String exampleName;

    /**
     * 初始化示例
     */
    public BaseExample(String exampleName) {
        this.exampleName = exampleName;
        log.info("初始化示例: {}", exampleName);
    }

    /**
     * 执行示例
     * 子类应实现此方法
     */
    public abstract void run();

    /**
     * 清理资源
     * 在示例执行完毕后调用
     */
    public void cleanup() {
        if (bot != null) {
            log.info("清理示例资源: {}", exampleName);
        }
    }

    /**
     * 带异常处理的运行方法
     */
    public void runSafely() {
        try {
            log.info("开始运行示例: {}", exampleName);
            run();
            log.info("示例运行完毕: {}", exampleName);
        } catch (Exception e) {
            log.error("示例执行出错: {}", exampleName, e);
        } finally {
            cleanup();
        }
    }

    /**
     * 获取示例名称
     * @return 示例名称
     */
    public String getExampleName() {
        return exampleName;
    }

    /**
     * 设置机器人实例
     * @param bot 机器人实例
     */
    public void setBot(AbstractPlatformBot bot) {
        this.bot = bot;
    }

    /**
     * 获取机器人实例
     * @return 机器人实例
     */
    public AbstractPlatformBot getBot() {
        return bot;
    }
}

