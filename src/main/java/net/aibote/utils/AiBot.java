package net.aibote.utils;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Data
public abstract class AiBot implements Runnable {

    protected static final Logger log = LoggerFactory.getLogger(AiBot.class);

    protected long waitTimeout = 5000;
    protected long intervalTimeout = 200;
    protected Socket clientCocket = null;

    @Override
    public void run() {
        webMain();
        close(); //脚本执行完毕，关闭通道
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private void close() {
        try {
            Thread.sleep(2000);
            this.clientCocket.shutdownOutput();
            this.clientCocket.shutdownInput();
            this.clientCocket.close();
            this.clientCocket = null;
            log.info("关闭通道");
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    public abstract void webMain();

    /**
     * 组织协议参数
     *
     * @param arrArgs
     * @return
     */
    protected String setSendData(String... arrArgs) {
        //数据格式 len/len/len\ndata
        StringBuilder strData = new StringBuilder();
        StringBuilder tempStr = new StringBuilder();
        for (String args : arrArgs) {
            if (args == null) args = "";
            tempStr.append(args);
            strData.append(args.getBytes(StandardCharsets.UTF_8).length);//获取包含中文实际长度
            strData.append('/');

        }
        strData.append('\n');
        strData.append(tempStr);
        return strData.toString();
    }

    /**
     * 发送协议到driver
     *
     * @param arrArgs
     * @return
     */
    protected synchronized String sendData(String... arrArgs) {
        try {
            String strData = setSendData(arrArgs);
            log.info("发送命令：" + strData);
            this.clientCocket.getOutputStream().write(strData.getBytes(StandardCharsets.UTF_8));
            this.clientCocket.getOutputStream().flush();
            InputStream inputStream = clientCocket.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            char[] buffer = new char[1024];
            int length = reader.read(buffer);
            String message = new String(buffer, 0, length);
            String[] ary = message.split("/", 2);
            int dataLen = Integer.valueOf(ary[0]);
            StringBuilder data = new StringBuilder(ary[1]);
            while (dataLen > data.toString().getBytes().length) {//如果包长度大于缓冲区，则继续读取。
                length = reader.read(buffer);
                message = new String(buffer, 0, length);
                data.append(message);
            }
            if ("null".contentEquals(data)) {
                return null;
            }
            return data.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean booleanCmd(String... arrArgs) {
        return "true".equals(this.sendData(arrArgs));
    }

    protected String strCmd(String... arrArgs) {
        return this.sendData(arrArgs);
    }

}
