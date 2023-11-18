package net.aibote.sdk;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
     * @param strData
     * @return
     */
    protected synchronized String sendData(String strData) {
        byte[] bytes = this.sendDataForBytes(strData);
        if (null == bytes) {
            return null;
        }
        return new String(bytes);
    }

    protected synchronized byte[] sendDataForBytes(String strData) {
        log.info("发送命令：" + strData);
        return sendBytes(strData.getBytes(StandardCharsets.UTF_8));
    }

    protected synchronized boolean sendFile(String functionName, String androidFilePath, byte[] fileData) {
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
            this.clientCocket.getOutputStream().write(inputBytes);
            this.clientCocket.getOutputStream().flush();

            InputStream inputStream = clientCocket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int readData = -1;
            while ((readData = inputStream.read()) != -1) {
                // 读取包结构，数据采用/分割。 返回数据格式为: dataLength/bytes
                if (readData != 47) { // 47为结构中的/
                    byteArrayOutputStream.write(readData);
                } else {
                    break; //读到分割符后跳出
                }
            }
            int dataLen = Integer.parseInt(byteArrayOutputStream.toString());
            byteArrayOutputStream.reset();//重置

            byte[] bytes = new byte[1024];
            int readLine = inputStream.read(bytes);
            byteArrayOutputStream.write(bytes, 0, readLine);//写入真实数据。根据实际读入长度写入数据。最大写入1024字节。
            while (dataLen > byteArrayOutputStream.toByteArray().length) {//如果包长度大于真实数据，则继续读取。
                readLine = inputStream.read(bytes);
                byteArrayOutputStream.write(bytes, 0, readLine);
            }

            return byteArrayOutputStream.toByteArray();
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
     * @return String
     */
    public String getVersion(){
        return "2023-11-18";
    }

}
