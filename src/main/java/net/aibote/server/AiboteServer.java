package net.aibote.server;

import net.aibote.sdk.AiBot;
import net.aibote.sdk.ChannelMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;

public abstract class AiboteServer {
    protected static final Logger log = LoggerFactory.getLogger(AiboteServer.class);



    public void startServer(Class<? extends AiBot> aiboteClass, int serverPort) {

        // 创建 Socket 服务端，并设置监听的端口
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            // 启动全局监控
            ChannelMap channelMap = ChannelMap.instance();
            new Thread(channelMap).start();
            // 服务器准备正式启动
            Thread thread;
            AiBot aiBot;
            String keyId;
            while (true) {
                // 阻塞方法，监听客户端请求
                Socket socket = serverSocket.accept();
                aiBot = aiboteClass.getDeclaredConstructor().newInstance();
                aiBot.setClientCocket(socket);
                // 启动后，加入全局监控
                keyId = socket.getInetAddress().getHostAddress();
                channelMap.put(keyId, aiBot);
                // 启动线程
                thread = Thread.ofVirtual().unstarted(aiBot);
                thread.start();
            }
        } catch (Exception ignored) {

        } finally {
            log.info("服务器关闭");
        }
    }

}
