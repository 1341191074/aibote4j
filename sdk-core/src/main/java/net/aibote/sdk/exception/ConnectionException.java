package net.aibote.sdk.exception;

/**
 * 连接异常
 *
 * 当建立、维护或关闭连接时发生错误时抛出
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
public class ConnectionException extends AiboteException {

    public static final int ERROR_CODE = 1001;
    public static final String ERROR_MESSAGE = "连接错误";

    /**
     * 创建连接异常
     *
     * @param message 具体错误描述
     */
    public ConnectionException(String message) {
        super(ERROR_CODE, ERROR_MESSAGE + ": " + message);
    }

    /**
     * 创建连接异常（带原因）
     *
     * @param message 具体错误描述
     * @param cause 原始异常
     */
    public ConnectionException(String message, Throwable cause) {
        super(ERROR_CODE, ERROR_MESSAGE + ": " + message, cause);
    }
}

