package net.aibote.sdk.protocol;

import net.aibote.sdk.protocol.impl.EnhancedProtocolImpl;
import net.aibote.sdk.protocol.impl.SimpleProtocolImpl;

/**
 * 通信协议工厂类
 * 用于创建不同类型的通信协议实例
 */
public class ProtocolFactory {
    
    /**
     * 协议类型枚举
     */
    public enum ProtocolType {
        SIMPLE_PROTOCOL,
        ENHANCED_PROTOCOL
    }
    
    /**
     * 创建协议实例
     * @param protocolType 协议类型
     * @return 通信协议实例
     */
    public static CommunicationProtocol createProtocol(ProtocolType protocolType) {
        switch (protocolType) {
            case SIMPLE_PROTOCOL:
                return new SimpleProtocolImpl();
            case ENHANCED_PROTOCOL:
                return new EnhancedProtocolImpl();
            default:
                throw new IllegalArgumentException("不支持的协议类型: " + protocolType);
        }
    }
    
    /**
     * 获取默认协议实例
     * @return 默认通信协议实例
     */
    public static CommunicationProtocol getDefaultProtocol() {
        return new EnhancedProtocolImpl(); // 使用增强版作为默认协议
    }
}