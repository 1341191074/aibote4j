package net.aibote.sdk.factory;

import net.aibote.sdk.AiBot;
import net.aibote.sdk.AndroidBot;
import net.aibote.sdk.WebBot;
import net.aibote.sdk.WinBot;

/**
 * Bot工厂类，用于创建不同类型的Bot实例
 */
public class BotFactory {
    
    /**
     * Bot类型枚举
     */
    public enum BotType {
        WIN_BOT,
        WEB_BOT,
        ANDROID_BOT
    }
    
    /**
     * 根据类型创建Bot实例
     * @param botType Bot类型
     * @return Bot实例
     */
    public static AiBot createBot(BotType botType) {
        switch (botType) {
            case WIN_BOT:
                return new WinBot() {
                    @Override
                    public void webMain() {
                        // 默认实现，具体逻辑应在子类中实现
                    }
                };
            case WEB_BOT:
                return new WebBot() {
                    @Override
                    public void webMain() {
                        // 默认实现，具体逻辑应在子类中实现
                    }
                };
            case ANDROID_BOT:
                return new AndroidBot() {
                    @Override
                    public void webMain() {
                        // 默认实现，具体逻辑应在子类中实现
                    }
                };
            default:
                throw new IllegalArgumentException("不支持的Bot类型: " + botType);
        }
    }
    
    /**
     * 根据类型创建Bot实例的泛型方法
     * @param botType Bot类型
     * @param botClass Bot类类型
     * @return Bot实例
     */
    @SuppressWarnings("unchecked")
    public static <T extends AiBot> T createBot(BotType botType, Class<T> botClass) {
        AiBot bot = createBot(botType);
        return (T) bot;
    }
    
    /**
     * 创建指定类型的WinBot实例
     * @param botClass 实现WinBot的具体类
     * @return WinBot实例
     */
    public static <T extends WinBot> T createWinBot(Class<T> botClass) {
        try {
            return botClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无法创建WinBot实例: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建指定类型的WebBot实例
     * @param botClass 实现WebBot的具体类
     * @return WebBot实例
     */
    public static <T extends WebBot> T createWebBot(Class<T> botClass) {
        try {
            return botClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无法创建WebBot实例: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建指定类型的AndroidBot实例
     * @param botClass 实现AndroidBot的具体类
     * @return AndroidBot实例
     */
    public static <T extends AndroidBot> T createAndroidBot(Class<T> botClass) {
        try {
            return botClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无法创建AndroidBot实例: " + e.getMessage(), e);
        }
    }
}