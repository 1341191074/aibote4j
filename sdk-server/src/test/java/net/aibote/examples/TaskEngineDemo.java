package net.aibote.examples;

import lombok.extern.slf4j.Slf4j;
import net.aibote.Application;
import net.aibote.task.TaskEngine;
import net.aibote.task.impl.NotepadAutomationTask;

/**
 * 任务引擎完整演示示例
 * 展示从任务注册到自动执行的完整流程
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class TaskEngineDemo extends BaseExample {

    public TaskEngineDemo() {
        super("TaskEngineDemo");
    }

    @Override
    public void run() {
        try {
            log.info("=== Aibote4J 任务引擎演示 ===");
            
            // 1. 展示任务注册
            demonstrateTaskRegistration();
            
            // 2. 展示任务查询
            demonstrateTaskQuery();
            
            // 3. 模拟应用启动流程
            demonstrateApplicationStartup();
            
            log.info("=== 演示完成 ===");
            log.info("实际使用时，运行 Application.main() 启动完整服务");
            
        } catch (Exception e) {
            log.error("演示执行失败", e);
            throw e;
        }
    }
    
    /**
     * 演示任务注册功能
     */
    private void demonstrateTaskRegistration() {
        log.info("--- 任务注册演示 ---");
        
        // 创建不同类型的任务
        NotepadAutomationTask task1 = NotepadAutomationTask.builder()
                .taskName("记事本任务1")
                .scriptName("Notepad-Task-1")
                .description("第一个记事本自动化任务")
                .build();
        
        NotepadAutomationTask task2 = NotepadAutomationTask.builder()
                .taskName("记事本任务2")
                .scriptName("Notepad-Task-2")
                .description("第二个记事本自动化任务")
                .build();
        
        // 注册任务
        String taskId1 = TaskEngine.getInstance().registerTask("notepad-1", task1);
        String taskId2 = TaskEngine.getInstance().registerTask("notepad-2", task2);
        
        log.info("已注册任务:");
        log.info("  - {}: {} (ID: {})", task1.getTaskName(), task1.getDescription(), taskId1);
        log.info("  - {}: {} (ID: {})", task2.getTaskName(), task2.getDescription(), taskId2);
    }
    
    /**
     * 演示任务查询功能
     */
    private void demonstrateTaskQuery() {
        log.info("--- 任务查询演示 ---");
        
        // 获取所有任务
        var allTasks = TaskEngine.getInstance().getAllTasks();
        log.info("当前注册的任务数量: {}", allTasks.size());
        
        // 查询特定任务
        var taskEntry = allTasks.entrySet().iterator().next();
        var task = TaskEngine.getInstance().getTask(taskEntry.getKey());
        if (task != null) {
            log.info("查询到任务: {} (支持的平台: {})", 
                    task.getTaskName(), 
                    task.getSupportedBotTypes());
        }
    }
    
    /**
     * 演示应用启动流程
     */
    private void demonstrateApplicationStartup() {
        log.info("--- 应用启动流程演示 ---");
        
        log.info("1. 任务引擎已初始化");
        log.info("2. 默认任务已注册");
        log.info("3. 服务端准备启动");
        log.info("4. 客户端连接后将自动执行任务");
        
        // 模拟启动流程的部分逻辑
        log.info("模拟启动Windows服务端...");
        log.info("Windows服务端将在端口 16999 监听");
        log.info("客户端连接后将自动执行已注册的任务");
    }

    public static void main(String[] args) {
        TaskEngineDemo demo = new TaskEngineDemo();
        demo.runSafely();
    }
}