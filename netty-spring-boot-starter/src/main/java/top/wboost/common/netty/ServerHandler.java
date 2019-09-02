package top.wboost.common.netty;

import top.wboost.common.base.enums.CharsetEnum;
import top.wboost.common.netty.handler.ProtocolHandler;
import top.wboost.common.netty.protocol.NettyProtocol;

public class ServerHandler extends ProtocolHandler<NettyProtocol> {

    @Override
    public void channelReadInternal(NettyProtocol read) throws Exception {
        System.out.println(new String(read.getContent(), CharsetEnum.UTF_8.getCharset()));
        write("success!");
    }
}
