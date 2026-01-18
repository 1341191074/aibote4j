package net.aibote.sdk;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 机器人基类（已废弃，请使用AbstractPlatformBot）
 * 
 * @deprecated 请使用 {@link AbstractPlatformBot} 作为基类
 */
@Slf4j
@Deprecated
public abstract class Aibote extends AbstractPlatformBot {
    public Aibote() {
        super();
    }

    public Aibote(ChannelHandlerContext aiboteChanel) {
        super(aiboteChanel);
    }

    @Override
    public abstract String getScriptName();

    @Override
    public abstract void doScript();
}

