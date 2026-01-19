package net.aibote.examples;

import lombok.extern.slf4j.Slf4j;
import net.aibote.sdk.factory.BotFactory;
import net.aibote.sdk.pool.ConnectionPoolManager;

/**
 * 综合使用示例
 * 展示如何结合使用工厂模式、连接池、Builder模式等设计模式
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class ComprehensiveExample extends BaseExample {

    public ComprehensiveExample() {
        super("ComprehensiveExample");
    }

    @Override
    public void run() {
        try {
            // 获取连接池管理器
            ConnectionPoolManager poolManager = ConnectionPoolManager.getInstance();
            log.info("连接池初始连接数: {}", poolManager.getConnectionCount());

            // 使用新的Builder模式创建不同类型的机器人
            var winBot = BotFactory.builder()
                    .withBotType(BotFactory.BotType.WIN)
                    .withChannelContext(null)
                    .withScriptName("WinBot-Comprehensive")
                    .build();

            var webBot = BotFactory.builder()
                    .withBotType(BotFactory.BotType.WEB)
                    .withChannelContext(null)
                    .withScriptName("WebBot-Comprehensive")
                    .build();

            var androidBot = BotFactory.builder()
                    .withBotType(BotFactory.BotType.ANDROID)
                    .withChannelContext(null)
                    .withScriptName("AndroidBot-Comprehensive")
                    .build();

            log.info("成功创建了WinBot、WebBot和AndroidBot实例");

            // 演示使用不同类型的机器人
            demonstrateWinBot(winBot);
            demonstrateWebBot(webBot);
            demonstrateAndroidBot(androidBot);

            log.info("综合示例执行完成");

        } catch (Exception e) {
            log.error("综合示例执行失败", e);
            throw e;
        }
    }
    
    /**
     * 演示WinBot的使用
     * @param winBot Windows机器人实例
     */
    private void demonstrateWinBot(Object winBot) {
        log.info("--- WinBot演示 ---");
        if (winBot != null) {
            log.info("脚本名称: {}", bot.getScriptName());
            // 实际使用时可以调用各种Windows操作方法
            // 如: bot.findWindow(...), bot.clickMouse(...), 等
        }
    }
    
    /**
     * 演示WebBot的使用
     * @param webBot Web机器人实例
     */
    private void demonstrateWebBot(Object webBot) {
        log.info("--- WebBot演示 ---");
        if (webBot != null) {
            log.info("Web机器人已准备就绪");
            // 实际使用时可以调用各种Web操作方法
            // 如: bot.navigate(...), bot.clickElement(...), 等
        }
    }
    
    /**
     * 演示AndroidBot的使用
     * @param androidBot Android机器人实例
     */
    private void demonstrateAndroidBot(Object androidBot) {
        log.info("--- AndroidBot演示 ---");
        if (androidBot != null) {
            log.info("Android机器人已准备就绪");
            // 实际使用时可以调用各种Android操作方法
            // 如: bot.click(...), bot.swipe(...), 等
        }
    }

    public static void main(String[] args) {
        ComprehensiveExample example = new ComprehensiveExample();
        example.runSafely();
    }
}