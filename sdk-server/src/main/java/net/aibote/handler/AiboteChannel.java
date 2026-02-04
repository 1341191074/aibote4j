package net.aibote.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import net.aibote.sdk.AbstractPlatformBot;
import net.aibote.utils.ClientType;

public class AiboteChannel {

    public ChannelHandlerContext aiboteChanel;

    @Getter
    @Setter
    public ClientType clientType;

    @Getter
    private AbstractPlatformBot aibote;

    public AiboteChannel(ChannelHandlerContext aiboteChanel) {
        this.aiboteChanel = aiboteChanel;
    }

    public void setAibote(AbstractPlatformBot aibote) {
        this.aibote = aibote;
        this.aibote.aiboteChanel = this.aiboteChanel;
    }

}
