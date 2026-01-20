package net.aibote.sdk.strategy.pattern;

import lombok.Data;

import java.time.LocalDateTime; /**
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
