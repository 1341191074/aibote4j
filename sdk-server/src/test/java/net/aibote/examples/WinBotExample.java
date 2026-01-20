package net.aibote.examples;

import lombok.extern.slf4j.Slf4j;
import net.aibote.sdk.factory.BotFactory;

/**
 * Windows机器人使用示例
 * 演示如何创建和使用Windows机器人实例
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class WinBotExample extends BaseExample {

    public WinBotExample() {
        super("WinBotExample");
    }

    @Override
    public void run() {
        try {
            // 使用新的Builder模式创建WinBot
            bot = BotFactory.builder()
                    .withBotType(BotFactory.BotType.WIN)
                    .withChannelContext(null) // 实际使用时从Netty连接中获取
                    .withScriptName("WinBot-Demo")
                    .build();

            log.info("Windows机器人创建成功");
            log.info("脚本名称: {}", bot.getScriptName());

            // 示例：查找窗口
            // String hwnd = bot.findWindow("Notepad", "无标题 - 记事本");
            // log.info("找到窗口句柄: {}", hwnd);

            // 示例：获取窗口位置
            // String pos = bot.getWindowPos(hwnd);
            // log.info("窗口位置: {}", pos);

        } catch (Exception e) {
            log.error("Windows机器人示例执行失败", e);
            throw e;
        }
    }

    public static void main(String[] args) {
        WinBotExample example = new WinBotExample();
        example.runSafely();
    }
}