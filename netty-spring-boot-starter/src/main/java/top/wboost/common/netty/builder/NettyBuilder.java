package top.wboost.common.netty.builder;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import top.wboost.common.netty.protocol.NettyConstant;

/**
 * @Auther: jwsun
 * @Date: 2019/1/31 14:22
 */
public class NettyBuilder {

    /**
     * 对于ChannelOption.SO_BACKLOG的解释：
     * 服务器端TCP内核维护有两个队列，我们称之为A、B队列。客户端向服务器端connect时，会发送带有SYN标志的包（第一次握手），服务器端
     * 接收到客户端发送的SYN时，向客户端发送SYN ACK确认（第二次握手），此时TCP内核模块把客户端连接加入到A队列中，然后服务器接收到
     * 客户端发送的ACK时（第三次握手），TCP内核模块把客户端连接从A队列移动到B队列，连接完成，应用程序的accept会返回。也就是说accept
     * 从B队列中取出完成了三次握手的连接。
     * A队列和B队列的长度之和就是backlog。当A、B队列的长度之和大于ChannelOption.SO_BACKLOG时，新的连接将会被TCP内核拒绝。
     * 所以，如果backlog过小，可能会出现accept速度跟不上，A、B队列满了，导致新的客户端无法连接。要注意的是，backlog对程序支持的
     * 连接数并无影响，backlog影响的只是还没有被accept取出的连接
     */
    public static ServerBootstrap option(ServerBootstrap serverBootstrap) {
        return serverBootstrap.option(ChannelOption.SO_BACKLOG, NettyConstant.MAX_BYTES)//设置TCP缓冲区
                .option(ChannelOption.SO_RCVBUF, NettyConstant.MAX_BYTES)//设置接受数据缓冲大小
                .option(ChannelOption.SO_SNDBUF, NettyConstant.MAX_BYTES)//设置发送数据缓冲大小
                .option(ChannelOption.SO_KEEPALIVE, true); //保持连接
    }

    public static Bootstrap option(Bootstrap bootstrap) {
        return bootstrap.option(ChannelOption.SO_BACKLOG, NettyConstant.MAX_BYTES)//设置TCP缓冲区
                .option(ChannelOption.SO_RCVBUF, NettyConstant.MAX_BYTES)//设置接受数据缓冲大小
                .option(ChannelOption.SO_SNDBUF, NettyConstant.MAX_BYTES)//设置发送数据缓冲大小
                .option(ChannelOption.SO_KEEPALIVE, true); //保持连接
    }
}
