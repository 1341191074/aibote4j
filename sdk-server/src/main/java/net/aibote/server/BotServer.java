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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class BotServer {
    public abstract int getPort();

    public abstract void handlers(ChannelPipeline pipeline);

    public void start() {
        // 使用命名线程工厂，便于调试和监控
        EventLoopGroup boss = new NioEventLoopGroup(1, new NamedThreadFactory("Netty-Boss"));
        EventLoopGroup worker = new NioEventLoopGroup(0, new NamedThreadFactory("Netty-Worker")); // 0表示使用默认线程数
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(NioChannelOption.SO_BACKLOG, 1024)
                    .childOption(NioChannelOption.SO_KEEPALIVE, true)
                    .childOption(NioChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new AiboteDecoder());
                            pipeline.addLast("encoder", new AiboteEncoder());
                            handlers(pipeline);//注入自定义处理类
                        }
                    });
            
            log.info("Netty server starting on port {}", getPort());
            ChannelFuture future = bootstrap.bind(getPort()).sync();
            log.info("Netty server started successfully on port {}", getPort());
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Netty server startup failed on port {}", getPort(), e);
        } finally {
            gracefulShutdown(worker, boss);
        }
    }
    
    /**
     * 优雅关闭Netty服务
     */
    private void gracefulShutdown(EventLoopGroup worker, EventLoopGroup boss) {
        try {
            log.info("开始关闭Netty服务...");
            
            if (worker != null) {
                worker.shutdownGracefully().sync();
            }
            if (boss != null) {
                boss.shutdownGracefully().sync();
            }
            
            log.info("Netty服务已关闭");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Netty服务关闭被中断");
        }
    }
    
    /**
     * 命名线程工厂
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        
        public NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
