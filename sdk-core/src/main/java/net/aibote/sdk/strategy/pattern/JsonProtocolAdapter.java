package net.aibote.sdk.strategy.pattern;

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

