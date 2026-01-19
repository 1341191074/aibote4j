package net.aibote.sdk.exception;

/**
 * AIBoTe框架基础异常类
 *
 * 所有业务相关异常的父类，提供统一的异常处理机制
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
public class AiboteException extends RuntimeException {

    private final int errorCode;
    private final String errorMessage;
    private final long timestamp;

    /**
     * 创建异常实例
     *
     * @param errorCode 错误代码
     * @param message 错误信息
     */
    public AiboteException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建异常实例（带原因）
     *
     * @param errorCode 错误代码
     * @param message 错误信息
     * @param cause 原始异常
     */
    public AiboteException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取错误代码
     *
     * @return 错误代码
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取异常发生时间戳
     *
     * @return 毫秒时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 生成友好的错误描述
     *
     * @return 格式化的错误描述
     */
    @Override
    public String toString() {
        return String.format("[AiboteException] Code: %d, Message: %s, Time: %d",
                errorCode, errorMessage, timestamp);
    }
}

