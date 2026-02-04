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
         * 构建机器人实例（返回具体类型）
         * @param <T> 机器人类型
         * @return 机器人实例
         */
        @SuppressWarnings("unchecked")
        public <T extends AbstractPlatformBot> T build() {
            if (botType == null) {
                throw new AiboteException(1001, "机器人类型不能为空");
            }
            if (ctx == null) {
                log.warn("Channel context is null, creating bot without context");
            }

            AbstractPlatformBot bot = BotFactory.createBot(botType, ctx, scriptName);
            return (T) bot;
        }
        
        /**
         * 构建机器人实例（指定具体类型）
         * @param botClass 机器人类型Class
         * @param <T> 机器人类型
         * @return 机器人实例
         */
        public <T extends AbstractPlatformBot> T buildWithType(Class<T> botClass) {
            if (botClass == null) {
                throw new AiboteException(1001, "机器人类型Class不能为空");
            }
            if (ctx == null) {
                log.warn("Channel context is null, creating bot without context");
            }

            return BotFactory.createBot(botClass, ctx, scriptName);
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
     * 快速创建Windows机器人
     * @param ctx 通道上下文
     * @return WinBot实例
     */
    public static WinBot createWinBot(ChannelHandlerContext ctx) {
        return createBot(WinBot.class, ctx);
    }
    
    /**
     * 快速创建Windows机器人（带脚本名称）
     * @param ctx 通道上下文
     * @param scriptName 脚本名称
     * @return WinBot实例
     */
    public static WinBot createWinBotWithName(ChannelHandlerContext ctx, String scriptName) {
        return createBot(WinBot.class, ctx, scriptName);
    }
    
    /**
     * 快速创建Web机器人
     * @param ctx 通道上下文
     * @return WebBot实例
     */
    public static WebBot createWebBot(ChannelHandlerContext ctx) {
        return createBot(WebBot.class, ctx);
    }
    
    /**
     * 快速创建Web机器人（带脚本名称）
     * @param ctx 通道上下文
     * @param scriptName 脚本名称
     * @return WebBot实例
     */
    public static WebBot createWebBotWithName(ChannelHandlerContext ctx, String scriptName) {
        return createBot(WebBot.class, ctx, scriptName);
    }
    
    /**
     * 快速创建Android机器人
     * @param ctx 通道上下文
     * @return AndroidBot实例
     */
    public static AndroidBot createAndroidBot(ChannelHandlerContext ctx) {
        return createBot(AndroidBot.class, ctx);
    }
    
    /**
     * 快速创建Android机器人（带脚本名称）
     * @param ctx 通道上下文
     * @param scriptName 脚本名称
     * @return AndroidBot实例
     */
    public static AndroidBot createAndroidBotWithName(ChannelHandlerContext ctx, String scriptName) {
        return createBot(AndroidBot.class, ctx, scriptName);
    }
    
    /**
     * 泛型创建机器人实例
     * @param botClass 机器人类型Class
     * @param ctx 通道上下文
     * @param <T> 机器人类型
     * @return 机器人实例
     */
    public static <T extends AbstractPlatformBot> T createBot(Class<T> botClass, ChannelHandlerContext ctx) {
        return createBot(botClass, ctx, null);
    }

    /**
     * 泛型创建机器人实例（支持自定义脚本名称）
     * @param botClass 机器人类型Class
     * @param ctx 通道上下文
     * @param scriptName 脚本名称
     * @param <T> 机器人类型
     * @return 机器人实例
     */
    public static <T extends AbstractPlatformBot> T createBot(Class<T> botClass, ChannelHandlerContext ctx, String scriptName) {
        if (botClass == null) {
            throw new AiboteException(1001, "机器人类型Class不能为空");
        }

        try {
            T bot = botClass.getDeclaredConstructor().newInstance();
            
            // 设置通道上下文（如果存在setter方法）
            try {
                java.lang.reflect.Field channelField = AbstractPlatformBot.class.getDeclaredField("aiboteChanel");
                channelField.setAccessible(true);
                channelField.set(bot, ctx);
            } catch (Exception e) {
                log.debug("无法设置通道上下文: {}", e.getMessage());
            }
            return bot;
        } catch (Exception e) {
            log.error("创建机器人实例失败, 类型: {}", botClass.getSimpleName(), e);
            throw new AiboteException(1001, "创建机器人实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建机器人实例（向后兼容方法）
     * @param botType 机器人类型
     * @param ctx 通道上下文
     * @return 机器人实例
     */
    public static AbstractPlatformBot createBot(BotType botType, ChannelHandlerContext ctx) {
        return createBot(botType, ctx, null);
    }

    /**
     * 创建机器人实例（支持自定义脚本名称，向后兼容）
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
                    return createWinBotWithName(ctx, scriptName);
                case WEB:
                    return createWebBotWithName(ctx, scriptName);
                case ANDROID:
                    return createAndroidBotWithName(ctx, scriptName);
                default:
                    throw new AiboteException(1001, "不支持的机器人类型: " + botType);
            }
        } catch (Exception e) {
            log.error("创建机器人实例失败, 类型: {}", botType, e);
            throw new AiboteException(1001, "创建机器人实例失败: " + e.getMessage(), e);
        }
    }
    

}