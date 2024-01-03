package net.aibote.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import net.aibote.codec.AiboteDecoder;
import net.aibote.codec.AiboteEncoder;

@Slf4j
public abstract class BotServer {
    public abstract int getPort();

    public abstract void handlers(ChannelPipeline pipeline);

    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)//
                    .channel(NioServerSocketChannel.class)//
                    .childOption(NioChannelOption.SO_KEEPALIVE, true)//
                    .childOption(NioChannelOption.TCP_NODELAY, true)//
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new AiboteDecoder());
                            pipeline.addLast("encoder", new AiboteEncoder());
                            handlers(pipeline);//注入自定义处理类
                        }
                    });
            log.info("netty server start。。");
            ChannelFuture future = bootstrap.bind(getPort()).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

}
