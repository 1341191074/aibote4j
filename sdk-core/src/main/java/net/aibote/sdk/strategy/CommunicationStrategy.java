package net.aibote.sdk.strategy;

import io.netty.channel.ChannelHandlerContext;

/**
 * 通信策略接口
 * 实现策略模式，用于处理不同类型的通信协议
 */
public interface CommunicationStrategy {
    
    /**
     * 发送命令
     * @param ctx 通道上下文
     * @param command 命令
     * @param params 参数
     */
    void sendCommand(ChannelHandlerContext ctx, String command, String... params);
    
    /**
     * 发送字节命令
     * @param ctx 通道上下文
     * @param data 数据
     */
    void sendBytes(ChannelHandlerContext ctx, byte[] data);
    
    /**
     * 处理响应
     * @param response 响应数据
     * @return 处理后的响应
     */
    Object handleResponse(byte[] response);
}