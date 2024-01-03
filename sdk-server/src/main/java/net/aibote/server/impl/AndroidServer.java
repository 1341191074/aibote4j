package net.aibote.server.impl;

import io.netty.channel.ChannelPipeline;
import net.aibote.handler.impl.AndroidHandler;
import net.aibote.server.BotServer;

public class AndroidServer extends BotServer {

    private static final AndroidServer instance = new AndroidServer();
    @Override
    public int getPort() {
        int port = 16997;
        return port;
    }

    @Override
    public void handlers(ChannelPipeline pipeline) {
        pipeline.addLast(new AndroidHandler());
    }

    public static BotServer getInstance() {
        return instance;
    }
}
