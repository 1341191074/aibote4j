package net.aibote.sdk.strategy.pattern;

/**
 * 适配器模式实现 - 用于支持多种通信协议
 *
 * 目的:
 * - 统一不同协议的接口
 * - 方便扩展新协议
 * - 隔离协议差异
 */

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

/**
 * 简单的二进制协议适配器
 */
public class BinaryProtocolAdapter implements ProtocolAdapter {

    private static final String PROTOCOL_NAME = "BINARY";
    private static final String VERSION = "1.0";
    private static final byte MAGIC_NUMBER = (byte) 0xAB;

    @Override
    public byte[] encode(String message) throws Exception {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("消息不能为空");
        }

        byte[] msgBytes = message.getBytes("UTF-8");
        byte[] result = new byte[msgBytes.length + 3]; // magic + length(2) + data

        result[0] = MAGIC_NUMBER;
        result[1] = (byte) ((msgBytes.length >> 8) & 0xFF);
        result[2] = (byte) (msgBytes.length & 0xFF);
        System.arraycopy(msgBytes, 0, result, 3, msgBytes.length);

        return result;
    }

    @Override
    public String decode(byte[] data) throws Exception {
        if (data == null || data.length < 3) {
            throw new IllegalArgumentException("数据长度不足");
        }

        if (data[0] != MAGIC_NUMBER) {
            throw new IllegalArgumentException("无效的协议头");
        }

        int length = ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
        if (data.length < length + 3) {
            throw new IllegalArgumentException("数据长度不匹配");
        }

        return new String(data, 3, length, "UTF-8");
    }

    @Override
    public boolean validate(byte[] data) {
        return data != null && data.length >= 3 && data[0] == MAGIC_NUMBER;
    }

    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }
}

/**
 * JSON协议适配器
 */
public class JsonProtocolAdapter implements ProtocolAdapter {

    private static final String PROTOCOL_NAME = "JSON";
    private static final String VERSION = "1.0";

    @Override
    public byte[] encode(String message) throws Exception {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("消息不能为空");
        }

        // 简单实现：直接编码为UTF-8字节
        return message.getBytes("UTF-8");
    }

    @Override
    public String decode(byte[] data) throws Exception {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("数据为空");
        }

        return new String(data, "UTF-8");
    }

    @Override
    public boolean validate(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }

        String str = new String(data);
        return (str.startsWith("{") && str.endsWith("}")) ||
               (str.startsWith("[") && str.endsWith("]"));
    }

    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }
}

/**
 * 协议工厂 - 创建合适的适配器
 */
public class ProtocolAdapterFactory {

    /**
     * 创建协议适配器
     */
    public static ProtocolAdapter createAdapter(String protocolName) {
        if (protocolName == null) {
            throw new IllegalArgumentException("协议名称不能为空");
        }

        return switch (protocolName.toUpperCase()) {
            case "BINARY" -> new BinaryProtocolAdapter();
            case "JSON" -> new JsonProtocolAdapter();
            default -> throw new IllegalArgumentException("不支持的协议: " + protocolName);
        };
    }
}

