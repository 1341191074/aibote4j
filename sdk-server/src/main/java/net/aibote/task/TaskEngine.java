package net.aibote.task;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import net.aibote.sdk.AbstractPlatformBot;
import net.aibote.sdk.factory.BotFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务执行引擎
 * 负责管理、调度和执行自动化任务
 * 支持任务注册、客户端连接触发执行等功能
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class TaskEngine {
    
    private static final TaskEngine INSTANCE = new TaskEngine();
    private final Map<String, TaskDefinition> registeredTasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdGenerator = new AtomicLong(1);
    
    private TaskEngine() {
        // 私有构造函数
    }
    
    /**
     * 获取单例实例
     * @return TaskEngine单例
     */
    public static TaskEngine getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册任务
     * @param taskName 任务名称
     * @param taskDefinition 任务定义
     * @return 任务ID
     */
    public String registerTask(String taskName, TaskDefinition taskDefinition) {
        String taskId = "task_" + taskIdGenerator.getAndIncrement();
        registeredTasks.put(taskId, taskDefinition);
        log.info("任务已注册: {} (ID: {})", taskName, taskId);
        return taskId;
    }
    
    /**
     * 获取注册的任务
     * @param taskId 任务ID
     * @return 任务定义
     */
    public TaskDefinition getTask(String taskId) {
        return registeredTasks.get(taskId);
    }
    
    /**
     * 移除任务
     * @param taskId 任务ID
     * @return 是否移除成功
     */
    public boolean removeTask(String taskId) {
        TaskDefinition removed = registeredTasks.remove(taskId);
        if (removed != null) {
            log.info("任务已移除: {} (ID: {})", removed.getTaskName(), taskId);
            return true;
        }
        return false;
    }
    
    /**
     * 获取所有注册的任务
     * @return 任务映射
     */
    public Map<String, TaskDefinition> getAllTasks() {
        return new ConcurrentHashMap<>(registeredTasks);
    }
    
    /**
     * 客户端连接时执行默认任务
     * @param ctx 客户端通道上下文
     * @param botType 机器人类型
     */
    public void executeDefaultTask(ChannelHandlerContext ctx, BotFactory.BotType botType) {
        // 查找默认任务（可以根据botType选择不同的默认任务）
        String defaultTaskId = findDefaultTaskId(botType);
        if (defaultTaskId != null) {
            executeTask(defaultTaskId, ctx, botType);
        } else {
            log.warn("未找到{}类型的默认任务", botType);
        }
    }
    
    /**
     * 执行指定任务
     * @param taskId 任务ID
     * @param ctx 客户端通道上下文
     * @param botType 机器人类型
     */
    public void executeTask(String taskId, ChannelHandlerContext ctx, BotFactory.BotType botType) {
        TaskDefinition taskDefinition = registeredTasks.get(taskId);
        if (taskDefinition == null) {
            log.error("未找到任务: {}", taskId);
            return;
        }
        
        try {
            log.info("开始执行任务: {} ({})", taskDefinition.getTaskName(), taskId);
            
            // 创建机器人实例
            AbstractPlatformBot bot = createBotInstance(botType, ctx, taskDefinition.getScriptName());
            
            // 执行任务
            taskDefinition.getTaskExecutor().execute(bot);
            
            log.info("任务执行完成: {} ({})", taskDefinition.getTaskName(), taskId);
        } catch (Exception e) {
            log.error("任务执行失败: {} ({})", taskDefinition.getTaskName(), taskId, e);
        }
    }
    
    /**
     * 创建机器人实例
     * @param botType 机器人类型
     * @param ctx 通道上下文
     * @param scriptName 脚本名称
     * @return 机器人实例
     */
    private AbstractPlatformBot createBotInstance(BotFactory.BotType botType, ChannelHandlerContext ctx, String scriptName) {
        switch (botType) {
            case WIN:
                return BotFactory.createWinBotWithName(ctx, scriptName);
            case WEB:
                return BotFactory.createWebBotWithName(ctx, scriptName);
            case ANDROID:
                return BotFactory.createAndroidBotWithName(ctx, scriptName);
            default:
                throw new IllegalArgumentException("不支持的机器人类型: " + botType);
        }
    }
    
    /**
     * 查找默认任务ID
     * @param botType 机器人类型
     * @return 默认任务ID
     */
    private String findDefaultTaskId(BotFactory.BotType botType) {
        // 简单实现：返回第一个匹配类型的任务
        // 实际应用中可以根据配置或策略选择默认任务
        for (Map.Entry<String, TaskDefinition> entry : registeredTasks.entrySet()) {
            if (entry.getValue().getSupportedBotTypes().contains(botType)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * 获取任务数量
     * @return 任务数量
     */
    public int getTaskCount() {
        return registeredTasks.size();
    }
    
    /**
     * 清空所有任务
     */
    public void clearAllTasks() {
        registeredTasks.clear();
        log.info("所有任务已清空");
    }
}