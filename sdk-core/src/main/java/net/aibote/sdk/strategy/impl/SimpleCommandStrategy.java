package net.aibote.sdk.strategy.impl;

import io.netty.channel.ChannelHandlerContext;
import net.aibote.sdk.strategy.CommunicationStrategy;

/**
 * 简单命令通信策略实现
 */
public class SimpleCommandStrategy implements CommunicationStrategy {

    @Override
    public void sendCommand(ChannelHandlerContext ctx, String command, String... params) {
        String[] fullCommand = new String[params.length + 1];
        fullCommand[0] = command;
        System.arraycopy(params, 0, fullCommand, 1, params.length);
        
        ctx.writeAndFlush(fullCommand);
    }

    @Override
    public void sendBytes(ChannelHandlerContext ctx, byte[] data) {
        ctx.writeAndFlush(data);
    }

    @Override
    public Object handleResponse(byte[] response) {
        return response;
    }
}