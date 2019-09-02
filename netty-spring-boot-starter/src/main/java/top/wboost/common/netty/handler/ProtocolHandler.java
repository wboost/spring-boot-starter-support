package top.wboost.common.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import top.wboost.common.base.enums.CharsetEnum;
import top.wboost.common.netty.protocol.NettyProtocol;

/**
 * @Auther: jwsun
 * @Date: 2019/1/11 22:19
 */
public abstract class ProtocolHandler<T extends NettyProtocol> extends ChannelInboundHandlerAdapter {

    ThreadLocal<ChannelHandlerContext> ctx = new ThreadLocal<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.ctx.set(ctx);
        T buf = (T) msg;
        channelReadInternal(buf);
    }

    public abstract void channelReadInternal(T read) throws Exception;

    public void write(byte[] content) throws Exception {
        getCtx().writeAndFlush(new NettyProtocol(content));
    }

    public void write(String content) throws Exception {
        getCtx().writeAndFlush(new NettyProtocol(content.getBytes(CharsetEnum.UTF_8.getCharset())));
    }

    public ChannelHandlerContext getCtx() {
        return this.ctx.get();
    }


}
