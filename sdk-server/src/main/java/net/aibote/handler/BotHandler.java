package net.aibote.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import net.aibote.sdk.factory.BotFactory;
import net.aibote.task.TaskEngine;

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
        aiboteChannel.getAibote().setRetBuffer(msg);
        System.out.println("收到信息："+new String(msg, StandardCharsets.UTF_8));
    }

    /**
     * 当客户连接服务端之后（打开链接） 获取客户端的channel，并且放到ChannelGroup中去进行管理
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();
        AiboteChannel aiboteChannel = new AiboteChannel(ctx);
        
        // 创建对应的机器人实例
        try {
            BotFactory.BotType botType = getBotType();
            switch (botType) {
                case WIN:
                    aiboteChannel.setAibote(net.aibote.sdk.factory.BotFactory.createWinBot(ctx));
                    break;
                case WEB:
                    aiboteChannel.setAibote(net.aibote.sdk.factory.BotFactory.createWebBot(ctx));
                    break;
                case ANDROID:
                    aiboteChannel.setAibote(net.aibote.sdk.factory.BotFactory.createAndroidBot(ctx));
                    break;
                default:
                    throw new IllegalArgumentException("不支持的机器人类型: " + botType);
            }
            log.info("创建{}机器人实例成功", botType.getDescription());
        } catch (Exception e) {
            log.error("创建机器人实例失败", e);
            // 即使创建失败也要添加到客户端管理器中
        }
        
        clientManager.add(channelId, aiboteChannel);
        log.info("新的链接： " + channelId);
        
        // 客户端连接后自动执行默认任务
        executeDefaultTask(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();
        clientManager.remove(channelId);
        log.info("链接断开：  " + channelId);
    }
    
    /**
     * 执行默认任务
     * @param ctx 通道上下文
     */
    private void executeDefaultTask(ChannelHandlerContext ctx) {
        try {
            log.info("执行默认任务, 停止5秒");
            Thread.sleep(5000);
            BotFactory.BotType botType = getBotType();
            TaskEngine.getInstance().executeDefaultTask(ctx, botType);
        } catch (Exception e) {
            log.error("执行默认任务失败", e);
        }
    }
    
    /**
     * 获取机器人类型
     * @return 机器人类型
     */
    protected abstract BotFactory.BotType getBotType();
}
