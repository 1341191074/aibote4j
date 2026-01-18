package net.aibote.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 平台特定的Bot抽象基类，扩展AiBot的功能
 */
public abstract class AbstractPlatformBot extends AiBot {

    protected static final Logger log = LoggerFactory.getLogger(AbstractPlatformBot.class);

    protected AbstractPlatformBot() {
        super();
    }

    /**
     * 获取平台名称
     *
     * @return 平台名称
     */
    public abstract String getPlatformName();

    /**
     * 执行平台特定的初始化
     */
    protected abstract void platformInitialize();

    /**
     * 执行平台特定的清理工作
     */
    protected abstract void platformCleanup();
}