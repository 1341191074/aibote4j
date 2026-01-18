package net.aibote.sdk.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 通信协议接口，定义数据发送和接收的标准方法
 */
public interface CommunicationProtocol {

    /**
     * 发送数据
     *
     * @param outputStream 输出流
     * @param data         待发送的数据
     * @throws IOException IO异常
     */
    void sendData(OutputStream outputStream, byte[] data) throws IOException;

    /**
     * 接收数据
     *
     * @param inputStream 输入流
     * @return 接收到的数据
     * @throws IOException IO异常
     */
    byte[] receiveData(InputStream inputStream) throws IOException;

    /**
     * 获取协议名称
     *
     * @return 协议名称
     */
    String getProtocolName();
}