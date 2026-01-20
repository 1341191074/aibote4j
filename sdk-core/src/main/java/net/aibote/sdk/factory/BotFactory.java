package net.aibote.sdk.factory;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import net.aibote.sdk.AbstractPlatformBot;
import net.aibote.sdk.AndroidBot;
import net.aibote.sdk.WebBot;
import net.aibote.sdk.WinBot;
import net.aibote.sdk.exception.AiboteException;

/**
 * 机器人工厂类
 * 实现工厂模式和建造者模式，用于创建不同类型的机器人实例
 * 提供灵活的配置和实例化方式
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Slf4j
public class BotFactory {
    
    /**
     * 机器人类型枚举
     */
    public enum BotType {
        WIN("Windows机器人"),
        WEB("Web机器人"),
        ANDROID("Android机器人");

        private final String description;

        BotType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 机器人配置构建器
     */
    public static class BotConfigBuilder {
        private BotType botType;
        private ChannelHandlerContext ctx;
        private String scriptName;

        public BotConfigBuilder withBotType(BotType botType) {
            this.botType = botType;
            return this;
        }

        public BotConfigBuilder withChannelContext(ChannelHandlerContext ctx) {
            this.ctx = ctx;
            return this;
        }

        public BotConfigBuilder withScriptName(String scriptName) {
            this.scriptName = scriptName;
            return this;
        }

        /**
         * 构建机器人实例
         * @return 机器人实例
         */
        public AbstractPlatformBot build() {
            if (botType == null) {
                throw new AiboteException(1001, "机器人类型不能为空");
            }
            if (ctx == null) {
                log.warn("Channel context is null, creating bot without context");
            }

            return BotFactory.createBot(botType, ctx, scriptName);
        }
    }

    /**
     * 创建新的配置构建器
     * @return 配置构建器
     */
    public static BotConfigBuilder builder() {
        return new BotConfigBuilder();
    }
    
    /**
     * 创建机器人实例
     * @param botType 机器人类型
     * @param ctx 通道上下文
     * @return 机器人实例
     */
    public static AbstractPlatformBot createBot(BotType botType, ChannelHandlerContext ctx) {
        return createBot(botType, ctx, null);
    }

    /**
     * 创建机器人实例（支持自定义脚本名称）
     * @param botType 机器人类型
     * @param ctx 通道上下文
     * @param scriptName 脚本名称
     * @return 机器人实例
     */
    private static AbstractPlatformBot createBot(BotType botType, ChannelHandlerContext ctx, String scriptName) {
        if (botType == null) {
            throw new AiboteException(1001, "机器人类型不能为空");
        }

        try {
            switch (botType) {
                case WIN:
                    return createWinBot(ctx, scriptName);
                case WEB:
                    return createWebBot(ctx, scriptName);
                case ANDROID:
                    return createAndroidBot(ctx, scriptName);
                default:
                    throw new AiboteException(1001, "不支持的机器人类型: " + botType);
            }
        } catch (Exception e) {
            log.error("创建机器人实例失败, 类型: {}", botType, e);
            throw new AiboteException(1001, "创建机器人实例失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建Windows机器人
     * @param ctx 通道上下文
     * @param scriptName 脚本名称
     * @return Windows机器人实例
     */
    private static WinBot createWinBot(ChannelHandlerContext ctx, String scriptName) {
        final String finalScriptName = scriptName != null ? scriptName : "WinBot-" + System.currentTimeMillis();

        return new WinBot() {
            @Override
            public String getScriptName() {
                return finalScriptName;
            }

            @Override
            public void doScript() {
                // 默认实现
            }
        };
    }
    
    /**
     * 创建Web机器人
     * @param ctx 通道上下文
     * @param scriptName 脚本名称
     * @return Web机器人实例
     */
    private static WebBot createWebBot(ChannelHandlerContext ctx, String scriptName) {
        final String finalScriptName = scriptName != null ? scriptName : "WebBot-" + System.currentTimeMillis();

        return new WebBot() {
            @Override
            public String getScriptName() {
                return finalScriptName;
            }

            @Override
            public void doScript() {
                // 默认实现
            }
        };
    }
    
    /**
     * 创建Android机器人
     * @param ctx 通道上下文
     * @param scriptName 脚本名称
     * @return Android机器人实例
     */
    private static AndroidBot createAndroidBot(ChannelHandlerContext ctx, String scriptName) {
        final String finalScriptName = scriptName != null ? scriptName : "AndroidBot-" + System.currentTimeMillis();

        return new AndroidBot() {
            @Override
            public String getScriptName() {
                return finalScriptName;
            }

            @Override
            public void doScript() {
                // 默认实现
            }
        };
    }
}