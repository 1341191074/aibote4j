package net.aibote.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class AiboteDecoder extends ByteToMessageDecoder {
    /**
     * 服务端收到客户端发来的协议内容，进行解码
     * 只要不out.add ， 本次通讯累计的字节都会累加到in中。
     * 另外使用getBytes获取，如果是readBytes，则会增量获取数据。 get不会移动指针
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in  the {@link ByteBuf} from which to read data
     * @param out the {@link List} to which decoded messages should be added
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        int maxLen = in.readableBytes();//获取本次缓冲区总长度

        /*
        先读取7个字节。 为了保护服务器，传输内容最大不能超过9999999个字节包(约10M)
         */
        byte[] resultByte = new byte[7];
        in.getBytes(0, resultByte); //get不会移动指针
        int size = resultByte.length;
        int delimiterIndex = -1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < size; i++) {
            if (resultByte[i] == 47) //寻找协议头
            {
                delimiterIndex = i + 1;
                break;
            }
            baos.write(resultByte[i]);
        }
        int packLen = Integer.valueOf(new String(baos.toByteArray())); //找到包体的长度
        if (maxLen >= packLen + delimiterIndex) {
            in.skipBytes(delimiterIndex);//跳过包头
            resultByte = new byte[packLen];//重新分配读取的缓冲区，传递到handle
            in.readBytes(resultByte);//读取数据。并移动in的指针
            out.add(resultByte);
        }
    }
}
