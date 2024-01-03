package net.aibote.server.impl;

import io.netty.channel.ChannelPipeline;
import net.aibote.handler.impl.WinHandler;
import net.aibote.server.BotServer;

public class WinServer extends BotServer {

    private static final WinServer instance = new WinServer();
    @Override
    public int getPort() {
        int port = 16999;
        return port;
    }

    @Override
    public void handlers(ChannelPipeline pipeline) {
        pipeline.addLast(new WinHandler());
    }

    public static BotServer getInstance() {
        return instance;
    }
}
