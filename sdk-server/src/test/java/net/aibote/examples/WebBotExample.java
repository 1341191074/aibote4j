package net.aibote.examples;

import lombok.extern.slf4j.Slf4j;
import net.aibote.sdk.factory.BotFactory;

/**
 * Web机器人使用示例
 * 演示如何创建和使用Web机器人实例
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class WebBotExample extends BaseExample {

    public WebBotExample() {
        super("WebBotExample");
    }

    @Override
    public void run() {
        try {
            // 使用新的Builder模式创建WebBot
            bot = BotFactory.builder()
                    .withBotType(BotFactory.BotType.WEB)
                    .withChannelContext(null) // 实际使用时从Netty连接中获取
                    .withScriptName("WebBot-Demo")
                    .build();

            log.info("Web机器人创建成功");
            log.info("脚本名称: {}", bot.getScriptName());

            // 示例：导航到网页
            // boolean success = bot.navigate("https://www.example.com");
            // log.info("导航结果: {}", success);

            // 示例：获取页面标题
            // String title = bot.getTitle();
            // log.info("页面标题: {}", title);

        } catch (Exception e) {
            log.error("Web机器人示例执行失败", e);
            throw e;
        }
    }

    public static void main(String[] args) {
        WebBotExample example = new WebBotExample();
        example.runSafely();
    }
}