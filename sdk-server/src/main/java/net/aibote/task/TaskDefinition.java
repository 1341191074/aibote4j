package net.aibote.task;

import net.aibote.sdk.AbstractPlatformBot;
import net.aibote.sdk.factory.BotFactory;

import java.util.Set;

/**
 * 任务定义接口
 * 定义自动化任务的基本结构和执行逻辑
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
public interface TaskDefinition {
    
    /**
     * 获取任务名称
     * @return 任务名称
     */
    String getTaskName();
    
    /**
     * 获取脚本名称
     * @return 脚本名称
     */
    String getScriptName();
    
    /**
     * 获取支持的机器人类型
     * @return 支持的机器人类型集合
     */
    Set<BotFactory.BotType> getSupportedBotTypes();
    
    /**
     * 获取任务描述
     * @return 任务描述
     */
    String getDescription();
    
    /**
     * 获取任务执行器
     * @return 任务执行器
     */
    TaskExecutor getTaskExecutor();
    
    /**
     * 任务执行器接口
     */
    @FunctionalInterface
    interface TaskExecutor {
        /**
         * 执行任务
         * @param bot 机器人实例
         * @throws Exception 执行异常
         */
        void execute(AbstractPlatformBot bot) throws Exception;
    }
}