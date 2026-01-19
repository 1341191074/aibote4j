package net.aibote.examples;

import lombok.extern.slf4j.Slf4j;
import net.aibote.sdk.factory.BotFactory;

/**
 * Android机器人使用示例
 * 演示如何创建和使用Android机器人实例
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class AndroidBotExample extends BaseExample {

    public AndroidBotExample() {
        super("AndroidBotExample");
    }

    @Override
    public void run() {
        try {
            // 使用新的Builder模式创建AndroidBot
            bot = BotFactory.builder()
                    .withBotType(BotFactory.BotType.ANDROID)
                    .withChannelContext(null) // 实际使用时从Netty连接中获取
                    .withScriptName("AndroidBot-Demo")
                    .build();

            log.info("Android机器人创建成功");
            log.info("脚本名称: {}", bot.getScriptName());

            // 示例：获取屏幕大小
            // String size = bot.getWindowSize();
            // log.info("屏幕大小: {}", size);

        } catch (Exception e) {
            log.error("Android机器人示例执行失败", e);
            throw e;
        }
    }

    public static void main(String[] args) {
        AndroidBotExample example = new AndroidBotExample();
        example.runSafely();
    }
}