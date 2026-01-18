package net.aibote.sdk.protocol.impl;

import net.aibote.sdk.protocol.CommunicationProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 增强版通信协议实现
 * 提供异步处理能力和更好的性能优化
 */
public class EnhancedProtocolImpl implements CommunicationProtocol {
    
    private static final Logger log = LoggerFactory.getLogger(EnhancedProtocolImpl.class);
    private static final int BUFFER_SIZE = 8192; // 增加缓冲区大小
    
    @Override
    public void sendData(OutputStream outputStream, byte[] data) throws IOException {
        log.debug("发送命令字节数: {}", data.length);
        outputStream.write(data);
        outputStream.flush();
    }
    
    @Override
    public byte[] receiveData(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE]; // 使用更大的缓冲区
        
        // 读取长度信息
        StringBuilder lengthBuilder = new StringBuilder();
        int readData;
        while ((readData = inputStream.read()) != -1) {
            if (readData != 47) { // 47为字符'/'的ASCII码
                lengthBuilder.append((char) readData);
            } else {
                break; // 读到分割符后跳出
            }
        }
        
        int dataLen;
        try {
            dataLen = Integer.parseInt(lengthBuilder.toString());
        } catch (NumberFormatException e) {
            throw new IOException("无效的数据长度格式: " + lengthBuilder.toString(), e);
        }
        
        if (dataLen <= 0) {
            return new byte[0]; // 如果长度为0或负数，直接返回空数组
        }
        
        // 读取实际数据
        long totalBytesRead = 0;
        int bytesRead;
        
        while (totalBytesRead < dataLen) {
            int bytesToRead = (int) Math.min(buffer.length, dataLen - totalBytesRead);
            bytesRead = inputStream.read(buffer, 0, bytesToRead);
            
            if (bytesRead == -1) {
                break; // 流结束
            }
            
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
        
        if (totalBytesRead != dataLen) {
            log.warn("期望读取 {} 字节，实际读取 {} 字节", dataLen, totalBytesRead);
        }
        
        return byteArrayOutputStream.toByteArray();
    }
    
    /**
     * 异步发送数据
     * @param outputStream 输出流
     * @param data 数据
     * @return Future对象
     */
    public Future<Void> sendDataAsync(OutputStream outputStream, byte[] data) {
        return CompletableFuture.runAsync(() -> {
            try {
                sendData(outputStream, data);
            } catch (IOException e) {
                throw new RuntimeException("异步发送数据失败", e);
            }
        });
    }
    
    /**
     * 异步接收数据
     * @param inputStream 输入流
     * @return Future对象，包含接收到的数据
     */
    public Future<byte[]> receiveDataAsync(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return receiveData(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("异步接收数据失败", e);
            }
        });
    }
    
    @Override
    public String getProtocolName() {
        return "EnhancedProtocol";
    }
}