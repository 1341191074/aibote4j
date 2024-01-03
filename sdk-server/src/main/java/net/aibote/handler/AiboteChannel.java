package net.aibote.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import net.aibote.sdk.Aibote;
import net.aibote.utils.ClientType;

public class AiboteChannel {

    public ChannelHandlerContext aiboteChanel;

    @Getter
    @Setter
    public ClientType clientType;

    @Getter
    private Aibote aibote;

    public AiboteChannel(ChannelHandlerContext aiboteChanel) {
        this.aiboteChanel = aiboteChanel;
    }

    public void setAibote(Aibote aibote) {
        this.aibote = aibote;
        this.aibote.aiboteChanel = this.aiboteChanel;
    }

}
