package net.aibote.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import net.aibote.sdk.AbstractPlatformBot;

/**
 * Aibote通道封装类
 * 用于包装Netty ChannelHandlerContext并关联对应的机器人实例
 * 
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
public class AiboteChannel {

    /**
     * Netty通道上下文
     */
    public ChannelHandlerContext aiboteChanel;

    /**
     * 关联的机器人实例
     */
    @Getter
    private AbstractPlatformBot aibote;

    /**
     * 构造函数
     * @param aiboteChanel Netty通道上下文
     */
    public AiboteChannel(ChannelHandlerContext aiboteChanel) {
        this.aiboteChanel = aiboteChanel;
    }

    /**
     * 设置关联的机器人实例
     * @param aibote 机器人实例
     */
    public void setAibote(AbstractPlatformBot aibote) {
        this.aibote = aibote;
        // 同步设置机器人的通道上下文
        if (aibote != null) {
            aibote.aiboteChanel = this.aiboteChanel;
        }
    }
}