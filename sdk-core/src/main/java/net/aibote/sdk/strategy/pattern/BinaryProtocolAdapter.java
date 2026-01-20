package net.aibote.sdk.strategy.pattern;

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

