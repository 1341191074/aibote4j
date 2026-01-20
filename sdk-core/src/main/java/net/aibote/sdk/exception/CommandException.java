package net.aibote.sdk.exception;

/**
 * 命令执行异常
 *
 * 当执行机器人命令时发生错误时抛出
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
public class CommandException extends AiboteException {

    public static final int ERROR_CODE = 1002;
    public static final String ERROR_MESSAGE = "命令执行错误";

    /**
     * 创建命令异常
     *
     * @param message 具体错误描述
     */
    public CommandException(String message) {
        super(ERROR_CODE, ERROR_MESSAGE + ": " + message);
    }

    /**
     * 创建命令异常（带原因）
     *
     * @param message 具体错误描述
     * @param cause 原始异常
     */
    public CommandException(String message, Throwable cause) {
        super(ERROR_CODE, ERROR_MESSAGE + ": " + message, cause);
    }
}

