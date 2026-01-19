package net.aibote.sdk.strategy.pattern;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 命令模式实现 - 用于支持操作队列、撤销、重做等功能
 *
 * 优势:
 * - 解耦请求者和执行者
 * - 支持操作队列
 * - 支持操作撤销和重做
 * - 易于添加新操作
 */

/**
 * 命令接口
 */
public interface Command {

    /**
     * 执行命令
     */
    void execute() throws Exception;

    /**
     * 撤销命令
     */
    void undo() throws Exception;

    /**
     * 获取命令描述
     */
    String getDescription();

    /**
     * 检查命令是否可撤销
     */
    default boolean isUndoable() {
        return true;
    }
}

/**
 * 命令历史记录
 */
@Slf4j
public class CommandHistory {

    private final Deque<Command> undoStack = new LinkedList<>();
    private final Deque<Command> redoStack = new LinkedList<>();
    private static final int MAX_HISTORY = 100;

    /**
     * 执行命令
     */
    public void execute(Command command) throws Exception {
        if (command == null) {
            throw new IllegalArgumentException("命令不能为空");
        }

        try {
            command.execute();

            // 可撤销的命令才添加到历史
            if (command.isUndoable()) {
                undoStack.push(command);

                // 清空重做栈
                redoStack.clear();

                // 限制历史大小
                if (undoStack.size() > MAX_HISTORY) {
                    undoStack.removeLast();
                }
            }

            log.debug("命令已执行: {}", command.getDescription());
        } catch (Exception e) {
            log.error("命令执行失败: {}", command.getDescription(), e);
            throw e;
        }
    }

    /**
     * 撤销命令
     */
    public void undo() throws Exception {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("没有可撤销的命令");
        }

        Command command = undoStack.pop();
        try {
            command.undo();
            redoStack.push(command);
            log.debug("命令已撤销: {}", command.getDescription());
        } catch (Exception e) {
            log.error("命令撤销失败: {}", command.getDescription(), e);
            throw e;
        }
    }

    /**
     * 重做命令
     */
    public void redo() throws Exception {
        if (redoStack.isEmpty()) {
            throw new IllegalStateException("没有可重做的命令");
        }

        Command command = redoStack.pop();
        try {
            command.execute();
            undoStack.push(command);
            log.debug("命令已重做: {}", command.getDescription());
        } catch (Exception e) {
            log.error("命令重做失败: {}", command.getDescription(), e);
            throw e;
        }
    }

    /**
     * 检查是否可撤销
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * 检查是否可重做
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * 清空历史
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        log.debug("命令历史已清空");
    }

    /**
     * 获取历史大小
     */
    public int getHistorySize() {
        return undoStack.size();
    }
}

/**
 * 命令队列 - 批量执行命令
 */
@Slf4j
public class CommandQueue {

    private final BlockingQueue<Command> queue;
    private final ExecutorService executorService;
    private final CommandHistory history;
    private volatile boolean running = false;

    public CommandQueue(int queueSize) {
        this.queue = new LinkedBlockingQueue<>(queueSize);
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "CommandQueueExecutor");
            t.setDaemon(false);
            return t;
        });
        this.history = new CommandHistory();
    }

    /**
     * 启动队列处理
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;
        executorService.execute(() -> {
            while (running) {
                try {
                    Command command = queue.take();
                    history.execute(command);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("命令队列执行出错", e);
                }
            }
        });

        log.info("命令队列已启动");
    }

    /**
     * 停止队列处理
     */
    public void stop() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("命令队列已停止");
    }

    /**
     * 添加命令到队列
     */
    public void enqueue(Command command) throws InterruptedException {
        if (!running) {
            throw new IllegalStateException("命令队列未启动");
        }
        queue.put(command);
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * 获取历史记录
     */
    public CommandHistory getHistory() {
        return history;
    }
}

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

