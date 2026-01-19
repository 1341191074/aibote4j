package net.aibote.sdk.strategy.pattern;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件观察者模式实现
 *
 * 用途:
 * - 实现机器人状态变化通知
 * - 连接生命周期管理
 * - 命令执行监控
 */

/**
 * 事件类
 */
@Data
public class BotEvent {

    public enum EventType {
        // 连接事件
        CONNECTION_ESTABLISHED,
        CONNECTION_CLOSED,
        CONNECTION_FAILED,

        // 命令事件
        COMMAND_SENT,
        COMMAND_EXECUTED,
        COMMAND_FAILED,

        // 状态事件
        STATUS_CHANGED,
        ERROR_OCCURRED,

        // 性能事件
        PERFORMANCE_ISSUE,
        RECOVERY_SUCCESS
    }

    private EventType type;
    private String sourceId;      // 事件源ID
    private Object data;          // 事件数据
    private LocalDateTime timestamp;
    private String message;

    public BotEvent(EventType type, String sourceId, Object data, String message) {
        this.type = type;
        this.sourceId = sourceId;
        this.data = data;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}

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

/**
 * 事件管理器 - 实现观察者模式
 */
@Slf4j
public class EventManager {

    private static volatile EventManager instance;

    // 事件监听器映射：事件类型 -> 监听器列表
    private final Map<BotEvent.EventType, List<BotEventListener>> listeners =
            new ConcurrentHashMap<>();

    // 事件历史
    private final List<BotEvent> eventHistory = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY_SIZE = 1000;

    // 事件统计
    private final Map<BotEvent.EventType, Integer> eventStatistics = new ConcurrentHashMap<>();

    private EventManager() {
        // 初始化事件类型
        for (BotEvent.EventType type : BotEvent.EventType.values()) {
            listeners.put(type, new CopyOnWriteArrayList<>());
            eventStatistics.put(type, 0);
        }
    }

    /**
     * 获取单例
     */
    public static EventManager getInstance() {
        if (instance == null) {
            synchronized (EventManager.class) {
                if (instance == null) {
                    instance = new EventManager();
                }
            }
        }
        return instance;
    }

    /**
     * 注册事件监听器
     */
    public void addEventListener(BotEvent.EventType eventType, BotEventListener listener) {
        if (eventType == null || listener == null) {
            throw new IllegalArgumentException("事件类型和监听器都不能为空");
        }

        List<BotEventListener> listenerList = listeners.get(eventType);
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
            log.info("事件监听器已注册 - 类型: {}, 监听器: {}",
                    eventType, listener.getListenerName());
        }
    }

    /**
     * 移除事件监听器
     */
    public void removeEventListener(BotEvent.EventType eventType, BotEventListener listener) {
        if (eventType == null || listener == null) {
            return;
        }

        List<BotEventListener> listenerList = listeners.get(eventType);
        if (listenerList.remove(listener)) {
            log.info("事件监听器已移除 - 类型: {}, 监听器: {}",
                    eventType, listener.getListenerName());
        }
    }

    /**
     * 发布事件
     */
    public void publishEvent(BotEvent event) {
        if (event == null) {
            return;
        }

        // 记录到历史
        recordEventHistory(event);

        // 更新统计
        eventStatistics.merge(event.getType(), 1, Integer::sum);

        // 通知所有监听器
        List<BotEventListener> listenerList = listeners.get(event.getType());
        if (listenerList != null && !listenerList.isEmpty()) {
            for (BotEventListener listener : listenerList) {
                try {
                    if (listener.supports(event.getType())) {
                        listener.onEvent(event);
                    }
                } catch (Exception e) {
                    log.error("事件处理出错 - 监听器: {}, 事件: {}",
                            listener.getListenerName(), event.getType(), e);
                }
            }
        }

        log.debug("事件已发布 - 类型: {}, 源ID: {}, 消息: {}",
                event.getType(), event.getSourceId(), event.getMessage());
    }

    /**
     * 记录事件历史
     */
    private void recordEventHistory(BotEvent event) {
        eventHistory.add(event);
        if (eventHistory.size() > MAX_HISTORY_SIZE) {
            eventHistory.remove(0);
        }
    }

    /**
     * 获取事件历史
     */
    public List<BotEvent> getEventHistory(BotEvent.EventType eventType, int limit) {
        return eventHistory.stream()
                .filter(e -> eventType == null || e.getType() == eventType)
                .skip(Math.max(0, eventHistory.size() - limit))
                .toList();
    }

    /**
     * 获取事件统计
     */
    public Map<BotEvent.EventType, Integer> getEventStatistics() {
        return new HashMap<>(eventStatistics);
    }

    /**
     * 清空事件历史
     */
    public void clearEventHistory() {
        eventHistory.clear();
        log.info("事件历史已清空");
    }

    /**
     * 清空事件统计
     */
    public void clearEventStatistics() {
        eventStatistics.replaceAll((k, v) -> 0);
        log.info("事件统计已重置");
    }
}

