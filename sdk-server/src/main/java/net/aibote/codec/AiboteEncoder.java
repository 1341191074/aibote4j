package net.aibote.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class AiboteEncoder extends MessageToMessageEncoder<String[]> {

    private final Charset charset = StandardCharsets.UTF_8;

    /**
     * 服务端编码。 协议发送到客户端前，对发送数据进行编码处理
     *
     * @param ctx     the {@link ChannelHandlerContext} which this {@link MessageToMessageEncoder} belongs to
     * @param message the message to encode to an other one
     * @param out     the {@link List} into which the encoded msg should be added
     *                needs to do some kind of aggregation
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, String[] message, List<Object> out) {
        StringBuilder strData = new StringBuilder();
        StringBuilder tempStr = new StringBuilder();
        for (String msg : message) {
            tempStr.append(msg);
            strData.append(msg.getBytes().length);//获取包含中文实际长度
            strData.append('/');
        }

        strData.append('\n');
        strData.append(tempStr);
        out.add(strData.toString());
    }
}
