package net.aibote.examples;

import net.aibote.sdk.AiBot;
import net.aibote.sdk.AndroidBot;
import net.aibote.sdk.WebBot;
import net.aibote.sdk.WinBot;
import net.aibote.sdk.factory.BotFactory;
import net.aibote.sdk.pool.ClientConnectionPool;
import net.aibote.server.AiboteServer;

import java.util.concurrent.Future;

/**
 * AIBoat4J使用示例
 * 演示如何使用工厂模式创建Bot实例和使用连接池
 */
public class BotUsageExample {
    
    public static void main(String[] args) {
        System.out.println("=== AIBoat4J 使用示例 ===");
        
        // 示例1: 使用工厂模式创建不同类型的Bot
        demonstrateFactoryPattern();
        
        // 示例2: 使用连接池提交任务
        demonstrateConnectionPool();
        
        // 示例3: 实际Bot使用示例
        demonstrateRealUsage();
    }
    
    /**
     * 演示工厂模式的使用
     */
    private static void demonstrateFactoryPattern() {
        System.out.println("\n--- 演示工厂模式 ---");
        
        // 使用工厂创建Windows Bot
        AiBot winBot = BotFactory.createBot(BotFactory.BotType.WIN_BOT);
        System.out.println("创建的Bot类型: " + winBot.getPlatformName());
        
        // 使用工厂创建Web Bot
        AiBot webBot = BotFactory.createBot(BotFactory.BotType.WEB_BOT);
        System.out.println("创建的Bot类型: " + webBot.getPlatformName());
        
        // 使用工厂创建Android Bot
        AiBot androidBot = BotFactory.createBot(BotFactory.BotType.ANDROID_BOT);
        System.out.println("创建的Bot类型: " + androidBot.getPlatformName());
    }
    
    /**
     * 演示连接池的使用
     */
    private static void demonstrateConnectionPool() {
        System.out.println("\n--- 演示连接池 ---");
        
        // 获取连接池实例
        ClientConnectionPool pool = ClientConnectionPool.getInstance();
        
        // 提交多个任务到连接池
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            pool.submitBotTask(() -> {
                System.out.println("执行任务 " + taskId + ", 当前活跃连接数: " + pool.getActiveConnections());
                try {
                    // 模拟任务执行
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("任务 " + taskId + " 完成");
            });
        }
        
        // 显示池信息
        System.out.println("池信息: " + pool.getPoolInfo());
    }
    
    /**
     * 演示实际Bot使用
     */
    private static void demonstrateRealUsage() {
        System.out.println("\n--- 演示实际使用 ---");
        
        // 创建具体的Bot实现类
        WinBotExample winBotExample = new WinBotExample();
        WebBotExample webBotExample = new WebBotExample();
        AndroidBotExample androidBotExample = new AndroidBotExample();
        
        System.out.println("创建了具体的Bot实现类:");
        System.out.println("- Windows Bot实现: " + winBotExample.getPlatformName());
        System.out.println("- Web Bot实现: " + webBotExample.getPlatformName());
        System.out.println("- Android Bot实现: " + androidBotExample.getPlatformName());
    }
    
    /**
     * Windows Bot 示例实现
     */
    static class WinBotExample extends WinBot {
        @Override
        public void webMain() {
            // Windows自动化逻辑
            System.out.println("执行Windows自动化任务...");
        }
    }
    
    /**
     * Web Bot 示例实现
     */
    static class WebBotExample extends WebBot {
        @Override
        public void webMain() {
            // Web自动化逻辑
            System.out.println("执行Web自动化任务...");
        }
    }
    
    /**
     * Android Bot 示例实现
     */
    static class AndroidBotExample extends AndroidBot {
        @Override
        public void webMain() {
            // Android自动化逻辑
            System.out.println("执行Android自动化任务...");
        }
    }
}