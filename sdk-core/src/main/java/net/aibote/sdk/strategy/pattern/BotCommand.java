package net.aibote.sdk.strategy.pattern;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 具体命令示例 - 机器人命令
 */
@Slf4j
public class BotCommand implements Command {

    private final String commandType;
    private final String commandData;
    private String result;
    private final LocalDateTime createdAt;

    public BotCommand(String commandType, String commandData) {
        this.commandType = commandType;
        this.commandData = commandData;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public void execute() throws Exception {
        log.info("执行机器人命令: {}, 数据: {}", commandType, commandData);
        // 实际命令执行逻辑
        this.result = "命令执行结果";
    }

    @Override
    public void undo() throws Exception {
        log.info("撤销机器人命令: {}", commandType);
        this.result = null;
    }

    @Override
    public String getDescription() {
        return String.format("[%s] %s - %s", createdAt, commandType, commandData);
    }
}

