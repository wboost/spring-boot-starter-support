package top.wboost.common.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import top.wboost.common.netty.builder.NettyBuilder;
import top.wboost.common.netty.protocol.NettyConstant;
import top.wboost.common.netty.protocol.NettyProtocol;

import java.net.InetSocketAddress;

public class Client {

    public static void main(String[] args) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).handler(new LoggingHandler(LogLevel.INFO)).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            NettyProtocol.addHandler(socketChannel);
                            socketChannel.pipeline().addLast(new ClientHandler());
                        }
                    });
            ChannelFuture future = NettyBuilder.option(bootstrap).connect(new InetSocketAddress("127.0.0.1", 8765)).sync();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1024 - 8 - Unpooled.copiedBuffer(NettyConstant.END_DATA).array().length; i++) {
                sb.append('0');
            }
            /*for (int i = 0; i < (1); i++) {
                sb.append('0');
            }*/
            future.channel().writeAndFlush(new NettyProtocol((sb.toString()).getBytes()));
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}