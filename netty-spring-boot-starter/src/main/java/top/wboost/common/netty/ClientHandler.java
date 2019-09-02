package top.wboost.common.netty;

import top.wboost.common.base.enums.CharsetEnum;
import top.wboost.common.netty.handler.ProtocolHandler;
import top.wboost.common.netty.protocol.NettyProtocol;

public class ClientHandler extends ProtocolHandler<NettyProtocol> {

    @Override
    public void channelReadInternal(NettyProtocol read) {
        System.out.println(new String(read.getContent(), CharsetEnum.UTF_8.getCharset()));
    }

}
