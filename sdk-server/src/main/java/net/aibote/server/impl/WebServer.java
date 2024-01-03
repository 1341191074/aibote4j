package net.aibote.server.impl;

import io.netty.channel.ChannelPipeline;
import net.aibote.handler.impl.WebHandler;
import net.aibote.server.BotServer;

public class WebServer extends BotServer {

    private static final WebServer instance = new WebServer();

    @Override
    public int getPort() {
        int port = 16998;
        return port;
    }

    @Override
    public void handlers(ChannelPipeline pipeline) {
        pipeline.addLast(new WebHandler());
    }

    public static BotServer getInstance() {
        return instance;
    }
}
