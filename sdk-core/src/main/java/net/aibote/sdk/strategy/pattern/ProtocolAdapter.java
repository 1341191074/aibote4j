package net.aibote.sdk.strategy.pattern;

/**
 * 通信协议接口
 */
public interface ProtocolAdapter {

    /**
     * 编码消息
     */
    byte[] encode(String message) throws Exception;

    /**
     * 解码消息
     */
    String decode(byte[] data) throws Exception;

    /**
     * 验证消息格式
     */
    boolean validate(byte[] data);

    /**
     * 获取协议名称
     */
    String getProtocolName();

    /**
     * 获取协议版本
     */
    String getVersion();
}


