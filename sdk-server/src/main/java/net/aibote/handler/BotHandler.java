package net.aibote.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

//服务端代码
//服务端处理handler
@Slf4j
public abstract class BotHandler extends SimpleChannelInboundHandler<byte[]> {
    private ClientManager clientManager;

    public BotHandler() {
        clientManager = getClientManager();
    }

    public abstract ClientManager getClientManager();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] msg) {
        String channelId = ctx.channel().id().asLongText();
        AiboteChannel aiboteChannel = clientManager.get(channelId);
        byte[] bytes = (byte[]) msg;
        System.out.println(new String(bytes, StandardCharsets.UTF_8));
        ctx.writeAndFlush(new String[]{"i received msg"});
    }

    /**
     * 当客户连接服务端之后（打开链接） 获取客户端的channel，并且放到ChannelGroup中去进行管理
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();
        clientManager.add(channelId, new AiboteChannel(ctx));
        log.info("新的链接： " + channelId);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();
        clientManager.remove(channelId);
        log.info("链接断开：  " + channelId);
    }
}
