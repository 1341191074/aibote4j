package net.aibote.sdk.examples;

import io.netty.channel.ChannelHandlerContext;
import net.aibote.sdk.AndroidBot;
import net.aibote.sdk.WebBot;
import net.aibote.sdk.WinBot;
import net.aibote.sdk.factory.BotFactory;
import net.aibote.sdk.pool.ConnectionPoolManager;

/**
 * 综合使用示例
 * 展示如何结合使用工厂模式、连接池等设计模式
 */
public class ComprehensiveExample {
    
    public static void main(String[] args) {
        // 获取连接池管理器
        ConnectionPoolManager poolManager = ConnectionPoolManager.getInstance();
        
        System.out.println("连接池初始连接数: " + poolManager.getConnectionCount());
        
        // 模拟通道上下文（实际使用时会从Netty连接中获取）
        ChannelHandlerContext ctx = null; // 在实际环境中这应该来自Netty连接
        
        // 使用工厂创建不同类型的机器人
        WinBot winBot = BotFactory.createWinBot(ctx);
        WebBot webBot = BotFactory.createWebBot(ctx);
        AndroidBot androidBot = BotFactory.createAndroidBot(ctx);
        
        System.out.println("创建了WinBot、WebBot和AndroidBot实例");
        
        // 演示如何使用不同类型的机器人
        demonstrateWinBot(winBot);
        demonstrateWebBot(webBot);
        demonstrateAndroidBot(androidBot);
        
        System.out.println("综合示例执行完成");
    }
    
    /**
     * 演示WinBot的使用
     * @param winBot Windows机器人实例
     */
    private static void demonstrateWinBot(WinBot winBot) {
        System.out.println("\n--- WinBot演示 ---");
        System.out.println("脚本名称: " + winBot.getScriptName());
        // 实际使用时可以调用各种Windows操作方法
        // 如: winBot.findWindow(...), winBot.clickMouse(...), 等
    }
    
    /**
     * 演示WebBot的使用
     * @param webBot Web机器人实例
     */
    private static void demonstrateWebBot(WebBot webBot) {
        System.out.println("\n--- WebBot演示 ---");
        System.out.println("脚本名称: " + webBot.getScriptName());
        // 实际使用时可以调用各种Web操作方法
        // 如: webBot.navigate(...), webBot.clickElement(...), 等
    }
    
    /**
     * 演示AndroidBot的使用
     * @param androidBot Android机器人实例
     */
    private static void demonstrateAndroidBot(AndroidBot androidBot) {
        System.out.println("\n--- AndroidBot演示 ---");
        System.out.println("脚本名称: " + androidBot.getScriptName());
        // 实际使用时可以调用各种Android操作方法
        // 如: androidBot.click(...), androidBot.swipe(...), 等
    }
}