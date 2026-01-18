package net.aibote.sdk.examples;

import io.netty.channel.ChannelHandlerContext;
import net.aibote.sdk.WinBot;
import net.aibote.sdk.factory.BotFactory;

/**
 * Windows机器人使用示例
 */
public class WinBotExample {
    
    public static void main(String[] args) {
        // 模拟通道上下文（实际使用时会从Netty连接中获取）
        ChannelHandlerContext ctx = null; // 在实际环境中这应该来自Netty连接
        
        // 使用工厂创建WinBot实例
        WinBot winBot = BotFactory.createWinBot(ctx);
        
        // 示例：查找窗口
        // String hwnd = winBot.findWindow("Notepad", "无标题 - 记事本");
        // System.out.println("找到窗口句柄: " + hwnd);
        
        // 示例：获取窗口位置
        // String pos = winBot.getWindowPos(hwnd);
        // System.out.println("窗口位置: " + pos);
        
        System.out.println("WinBot示例初始化完成");
    }
}