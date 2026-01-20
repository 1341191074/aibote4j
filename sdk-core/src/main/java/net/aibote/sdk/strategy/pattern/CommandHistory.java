package net.aibote.sdk.strategy.pattern;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

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

