package net.aibote.sdk.protocol.impl;

import net.aibote.sdk.protocol.CommunicationProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 简单通信协议实现
 * 数据格式：len/len/len\ndata
 */
public class SimpleProtocolImpl implements CommunicationProtocol {
    
    private static final Logger log = LoggerFactory.getLogger(SimpleProtocolImpl.class);
    
    @Override
    public void sendData(OutputStream outputStream, byte[] data) throws IOException {
        log.info("发送命令：" + new String(data, StandardCharsets.UTF_8));
        outputStream.write(data);
        outputStream.flush();
    }
    
    @Override
    public byte[] receiveData(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int readData = -1;
        
        // 读取包结构，数据采用/分割。 返回数据格式为: dataLength/bytes
        while ((readData = inputStream.read()) != -1) {
            // 读取长度信息，直到遇到分隔符'/'
            if (readData != 47) { // 47为字符'/'的ASCII码
                byteArrayOutputStream.write(readData);
            } else {
                break; // 读到分割符后跳出
            }
        }
        
        int dataLen = Integer.parseInt(byteArrayOutputStream.toString());
        byteArrayOutputStream.reset(); // 重置输出流
        
        if (dataLen > 0) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            // 读取实际数据
            bytesRead = inputStream.read(buffer);
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            
            // 如果包长度大于当前读取的数据，则继续读取
            while (dataLen > byteArrayOutputStream.toByteArray().length) {
                bytesRead = inputStream.read(buffer);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        }
        
        return byteArrayOutputStream.toByteArray();
    }
    
    @Override
    public String getProtocolName() {
        return "SimpleProtocol";
    }
}