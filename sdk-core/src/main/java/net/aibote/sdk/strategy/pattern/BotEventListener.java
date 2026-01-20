package net.aibote.sdk.strategy.pattern;

/**
 * 事件观察者接口
 */
public interface BotEventListener {

    /**
     * 处理事件
     */
    void onEvent(BotEvent event);

    /**
     * 获取监听器名称
     */
    String getListenerName();

    /**
     * 检查是否应该处理该事件类型
     */
    boolean supports(BotEvent.EventType eventType);
}
