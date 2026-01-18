package net.aibote.sdk;

import lombok.Getter;
import net.aibote.sdk.protocol.CommunicationProtocol;
import net.aibote.sdk.protocol.ProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public abstract class AiBot implements Runnable {

    protected static final Logger log = LoggerFactory.getLogger(AiBot.class);

    protected long waitTimeout = 5000;
    protected long intervalTimeout = 200;
    protected Socket clientCocket = null;
    @Getter
    private String keyId = null;
    protected Map<String, Object> ymlConfig = null;
    
    // 通信协议策略
    protected CommunicationProtocol communicationProtocol = ProtocolFactory.getDefaultProtocol();

    public AiBot() {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("BotServer.yml");
        ymlConfig = yaml.load(inputStream);
        log.info("读取到配置文件" + ymlConfig.toString());
        try {
            assert inputStream != null;
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setClientCocket(Socket clientCocket) {
        this.clientCocket = clientCocket;
        keyId = clientCocket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        if (null != clientCocket && clientCocket.isConnected()) {
            try {
                webMain();
            } catch (RuntimeException e) {
                // nothing
            }
            close(); //脚本执行完毕，关闭通道
        }
    }

    protected void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private void close() {
        try {
            this.sleep(2000);
            this.clientCocket.shutdownOutput();
            this.clientCocket.shutdownInput();
            this.clientCocket.close();
            this.clientCocket = null;
            log.info("关闭通道");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public abstract void webMain() throws RuntimeException;

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
     * @param strData
     * @return
     */
    protected String sendData(String strData) {
        byte[] bytes = this.sendDataForBytes(strData);
        if (null == bytes) {
            return null;
        }
        return new String(bytes);
    }

    protected byte[] sendDataForBytes(String strData) {
        log.info("发送命令：" + strData);
        return sendBytes(strData.getBytes(StandardCharsets.UTF_8));
    }

    protected boolean sendFile(String functionName, String androidFilePath, byte[] fileData) {
        StringBuilder strData = new StringBuilder();
        strData.append(functionName.getBytes().length).append("/");
        strData.append(androidFilePath.getBytes().length).append("/");
        strData.append(fileData.length).append("/");
        strData.append(functionName);
        strData.append(androidFilePath);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(strData.toString().getBytes(StandardCharsets.UTF_8));
            byteArrayOutputStream.write(fileData);
            byte[] bytes = this.sendBytes(byteArrayOutputStream.toByteArray());
            return "true".equals(new String(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private byte[] sendBytes(byte[] inputBytes) {
        try {
            OutputStream outputStream = this.clientCocket.getOutputStream();
            InputStream inputStream = this.clientCocket.getInputStream();
            
            // 使用策略模式发送数据
            communicationProtocol.sendData(outputStream, inputBytes);
            
            // 使用策略模式接收数据
            return communicationProtocol.receiveData(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected byte[] bytesCmd(String... arrArgs) {
        String strData = this.setSendData(arrArgs);
        return this.sendDataForBytes(strData);
    }

    protected boolean booleanCmd(String... arrArgs) {
        String strData = this.setSendData(arrArgs);
        return "true".equals(this.sendData(strData));
    }

    protected boolean booleanDelayCmd(String... arrArgs) {
        String strData = this.setSendData(arrArgs);
        String retStr;
        long startTime = System.currentTimeMillis();
        long endTime;
        do {
            retStr = this.sendData(strData);
            if (null == retStr || "false".equals(retStr)) {
                this.sleep(this.intervalTimeout);
            } else {
                break;
            }
            endTime = System.currentTimeMillis();
        } while (endTime - startTime <= this.waitTimeout);

        return "true".equals(retStr);
    }

    protected String strCmd(String... arrArgs) {
        String strData = this.setSendData(arrArgs);
        strData = this.sendData(strData);
        if ("null".contentEquals(strData)) {
            return null;
        }
        return strData;
    }

    protected String strDelayCmd(String... arrArgs) {
        String strData = this.setSendData(arrArgs);
        String retStr;
        long startTime = System.currentTimeMillis();
        long endTime;
        do {
            retStr = this.sendData(strData);
            if ("null".equals(retStr) || null == retStr || "-1|-1".equals(retStr) || "-1.0|-1.0".equals(retStr)) {
                this.sleep(this.intervalTimeout);
            } else {
                break;
            }
            endTime = System.currentTimeMillis();
        } while (endTime - startTime <= this.waitTimeout);

        return retStr;
    }

    /**
     * 获取当前Aibote的版本号
     *
     * @return String
     */
    public String getVersion() {
        return "2023-11-25";
    }

}