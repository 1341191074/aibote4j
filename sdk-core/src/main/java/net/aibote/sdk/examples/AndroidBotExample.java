package net.aibote.sdk.examples;

import io.netty.channel.ChannelHandlerContext;
import net.aibote.sdk.AndroidBot;
import net.aibote.sdk.factory.BotFactory;

/**
 * Android机器人使用示例
 */
public class AndroidBotExample {
    
    public static void main(String[] args) {
        // 模拟通道上下文（实际使用时会从Netty连接中获取）
        ChannelHandlerContext ctx = null; // 在实际环境中这应该来自Netty连接
        
        // 使用工厂创建AndroidBot实例
        AndroidBot androidBot = BotFactory.createAndroidBot(ctx);
        
        // 示例：获取屏幕大小
        // String size = androidBot.getWindowSize();
        // System.out.println("屏幕大小: " + size);
        
        System.out.println("AndroidBot示例初始化完成");
    }
}