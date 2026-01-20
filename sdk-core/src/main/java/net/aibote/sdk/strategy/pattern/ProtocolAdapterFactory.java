package net.aibote.sdk.strategy.pattern;

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

