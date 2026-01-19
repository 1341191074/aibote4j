package net.aibote.sdk.strategy.pattern;

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

