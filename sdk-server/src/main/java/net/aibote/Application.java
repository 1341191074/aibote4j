package net.aibote;

import lombok.extern.slf4j.Slf4j;
import net.aibote.server.impl.AndroidServer;
import net.aibote.server.impl.WebServer;
import net.aibote.server.impl.WinServer;
import net.aibote.task.TaskEngine;
import net.aibote.task.impl.NotepadAutomationTask;

/**
 * Aibote4J 应用启动类
 * 演示任务引擎的完整使用流程
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class Application {
    
    public static void main(String[] args) {
        try {
            log.info("Aibote4J 服务端启动中...");
            
            // 注册JVM关闭钩子
            registerShutdownHook();
            
            // 1. 注册默认任务
            registerDefaultTasks();
            
            // 2. 启动所有服务端
            startAllServers();
            
            log.info("Aibote4J 服务端启动完成");
            log.info("客户端连接后将自动执行注册的任务");
            
            // 保持主线程运行
            synchronized (Application.class) {
                while (true) {
                    try {
                        Application.class.wait();
                    } catch (InterruptedException e) {
                        log.info("应用被中断，准备关闭...");
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("应用启动失败", e);
            System.exit(1);
        }
    }
    
    /**
     * 注册默认任务
     */
    private static void registerDefaultTasks() {
        log.info("注册默认任务...");
        
        // 注册记事本自动化任务
        NotepadAutomationTask notepadTask = NotepadAutomationTask.builder()
                .taskName("记事本自动化任务")
                .scriptName("Notepad-Automation")
                .description("自动查找记事本窗口并执行基本操作")
                .build();
        
        String taskId = TaskEngine.getInstance().registerTask("notepad-task", notepadTask);
        log.info("已注册任务: {} (ID: {})", notepadTask.getTaskName(), taskId);
        
        log.info("默认任务注册完成，当前任务总数: {}", TaskEngine.getInstance().getTaskCount());
    }
    
    /**
     * 注册JVM关闭钩子，确保优雅关闭
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("接收到关闭信号，开始优雅关闭...");
            
            try {
                // 关闭客户端管理器
                net.aibote.handler.ClientManager.getInstance().shutdown();
                log.info("客户端管理器已关闭");
                
                // 等待一段时间让剩余请求处理完成
                Thread.sleep(2000);
                
            } catch (Exception e) {
                log.error("关闭过程中出现异常", e);
            }
            
            log.info("应用程序已优雅关闭");
        }, "Shutdown-Hook"));
    }
    
    /**
     * 启动所有服务端
     */
    private static void startAllServers() {
        log.info("启动所有服务端...");
        
        // 启动Android服务端
        startServer("Android", AndroidServer.getInstance(), 16997);
        
        // 启动WebService端
        startServer("Web", WebServer.getInstance(), 16998);
        
        // 启动Windows服务端
        startServer("Windows", WinServer.getInstance(), 16999);
        
        log.info("所有服务端启动完成");
    }
    
    /**
     * 启动单个服务端
     */
    private static void startServer(String serverName, net.aibote.server.BotServer server, int port) {
        Thread serverThread = new Thread(() -> {
            try {
                log.info("{}服务端开始启动，端口: {}", serverName, port);
                server.start();
            } catch (Exception e) {
                log.error("{}服务端启动失败", serverName, e);
            }
        }, serverName + "Server-Thread");
        
        serverThread.setDaemon(false);
        serverThread.start();
        
        log.info("{}服务端已在端口 {} 启动", serverName, port);
    }
}