package net.aibote.sdk.exception;

/**
 * 超时异常
 *
 * 当等待响应超时时抛出
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
public class TimeoutException extends AiboteException {

    public static final int ERROR_CODE = 1003;
    public static final String ERROR_MESSAGE = "操作超时";

    /**
     * 创建超时异常
     *
     * @param message 具体错误描述
     */
    public TimeoutException(String message) {
        super(ERROR_CODE, ERROR_MESSAGE + ": " + message);
    }

    /**
     * 创建超时异常（带原因）
     *
     * @param message 具体错误描述
     * @param cause 原始异常
     */
    public TimeoutException(String message, Throwable cause) {
        super(ERROR_CODE, ERROR_MESSAGE + ": " + message, cause);
    }
}

