package net.aibote.examples;

import net.aibote.sdk.AiBot;
import net.aibote.sdk.WinBot;
import net.aibote.sdk.WebBot;
import net.aibote.sdk.AndroidBot;
import net.aibote.sdk.factory.BotFactory;
import net.aibote.sdk.pool.ClientConnectionPool;
import net.aibote.sdk.protocol.ProtocolFactory;
import net.aibote.sdk.protocol.CommunicationProtocol;

import java.util.concurrent.Future;

/**
 * AIBoat4J 实际应用示例
 * 展示在实际项目中如何使用各种功能
 */
public class PracticalUsageExample {
    
    public static void main(String[] args) {
        System.out.println("=== AIBoat4J 实际应用示例 ===");
        
        // 示例1: 协议选择
        demonstrateProtocolSelection();
        
        // 示例2: 高级Bot创建
        demonstrateAdvancedBotCreation();
        
        // 示例3: 并发处理示例
        demonstrateConcurrentProcessing();
    }
    
    /**
     * 演示协议选择
     */
    private static void demonstrateProtocolSelection() {
        System.out.println("\n--- 演示协议选择 ---");
        
        // 获取简单协议
        CommunicationProtocol simpleProtocol = ProtocolFactory.createProtocol(ProtocolFactory.ProtocolType.SIMPLE_PROTOCOL);
        System.out.println("创建简单协议: " + simpleProtocol.getProtocolName());
        
        // 获取增强协议
        CommunicationProtocol enhancedProtocol = ProtocolFactory.createProtocol(ProtocolFactory.ProtocolType.ENHANCED_PROTOCOL);
        System.out.println("创建增强协议: " + enhancedProtocol.getProtocolName());
        
        // 获取默认协议
        CommunicationProtocol defaultProtocol = ProtocolFactory.getDefaultProtocol();
        System.out.println("获取默认协议: " + defaultProtocol.getProtocolName());
    }
    
    /**
     * 演示高级Bot创建
     */
    private static void demonstrateAdvancedBotCreation() {
        System.out.println("\n--- 演示高级Bot创建 ---");
        
        // 使用工厂创建特定类型的Bot
        CustomWinBot customWinBot = BotFactory.createWinBot(CustomWinBot.class);
        System.out.println("创建自定义Windows Bot: " + customWinBot.getPlatformName());
        
        CustomWebBot customWebBot = BotFactory.createWebBot(CustomWebBot.class);
        System.out.println("创建自定义Web Bot: " + customWebBot.getPlatformName());
        
        CustomAndroidBot customAndroidBot = BotFactory.createAndroidBot(CustomAndroidBot.class);
        System.out.println("创建自定义Android Bot: " + customAndroidBot.getPlatformName());
    }
    
    /**
     * 演示并发处理
     */
    private static void demonstrateConcurrentProcessing() {
        System.out.println("\n--- 演示并发处理 ---");
        
        ClientConnectionPool pool = ClientConnectionPool.getInstance();
        
        // 提交不同类型的任务
        for (int i = 0; i < 3; i++) {
            final int taskId = i;
            Future<?> future = pool.submitBotTask(() -> {
                System.out.println("并发任务 " + taskId + " 开始执行");
                // 模拟处理时间
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("并发任务 " + taskId + " 执行完成");
                return "Task " + taskId + " result";
            });
            
            // 可以检查任务状态
            System.out.println("任务 " + taskId + " 是否完成: " + future.isDone());
        }
        
        // 显示池状态
        System.out.println("连接池状态: " + pool.getPoolInfo());
        System.out.println("排队任务数: " + pool.getQueuedTaskCount());
        System.out.println("已完成任务数: " + pool.getCompletedTaskCount());
    }
    
    /**
     * 自定义Windows Bot实现
     */
    public static class CustomWinBot extends WinBot {
        @Override
        public void webMain() {
            System.out.println("自定义Windows Bot执行主要逻辑");
            // 实现具体业务逻辑
        }
    }
    
    /**
     * 自定义Web Bot实现
     */
    public static class CustomWebBot extends WebBot {
        @Override
        public void webMain() {
            System.out.println("自定义Web Bot执行主要逻辑");
            // 实现具体业务逻辑
        }
    }
    
    /**
     * 自定义Android Bot实现
     */
    public static class CustomAndroidBot extends AndroidBot {
        @Override
        public void webMain() {
            System.out.println("自定义Android Bot执行主要逻辑");
            // 实现具体业务逻辑
        }
    }
}