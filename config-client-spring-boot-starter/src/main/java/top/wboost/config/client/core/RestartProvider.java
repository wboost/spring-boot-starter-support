package top.wboost.config.client.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.ResponseEntity;
import top.wboost.common.base.entity.HttpRequestBuilder;
import top.wboost.common.constant.Global;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.netty.handler.ProtocolHandler;
import top.wboost.common.netty.protocol.NettyProtocol;
import top.wboost.common.util.HttpClientUtil;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static top.wboost.config.client.core.ConfigClientRegister.REQUEST_END;

/**
 * @Auther: jwsun
 * @Date: 2019/1/3 18:39
 */
public class RestartProvider implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerUtil.getLogger(NettyContainer.class);
    List<RestartObserver> restartObservers;
    @Value("${netty.socket.port}")
    private int socketPort;
    @Value("${spring.cloud.client.ipAddress}")
    private String ipAddress;
    private NettyContainer nettyContainer;
    // applicationName:AppInstance
    // private Map<String,AppInstance> instanceMap = new ConcurrentHashMap<>();

    public RestartProvider(List<RestartObserver> restartObservers) {
        this.restartObservers = restartObservers;
        //this.nettyContainer = new NettyContainer(socketPort, restartObservers);
    }

    public RestartStatus restartApp(String ip, int port, String contex, boolean wait) {
        restartByInstance(ip, port, contex);
        return new RestartStatus();
    }

    public ResponseEntity<String> restartByInstance(String ip, int port, String context) {
        return HttpClientUtil.execute(HttpRequestBuilder.post(getRestartUrl(ip, port, context)).addParameter("socket_ip", ipAddress).addParameter("socket_port", String.valueOf(socketPort)));
    }

    private String getRestartUrl(String ip, int port, String context) {
        return (context != null ? "http://" + ip + ":" + port + "/" + context + ConfigClientRegister.WEB_PREFIX
                : "http://" + ip + ":" + port + ConfigClientRegister.WEB_PREFIX) + "/restart";
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        /*if (event instanceof ContextClosedEvent) {
            this.nettyContainer.stopSocket();
        } else if (event instanceof ContextStartedEvent) {
            this.nettyContainer.startSocket();
        }*/
        if (event instanceof ContextClosedEvent) {
            this.nettyContainer.stopSocket();
        } else if (event instanceof ContextRefreshedEvent) {
            this.nettyContainer = new NettyContainer(socketPort, restartObservers);
            this.nettyContainer.startSocket();
        }
    }

    public static class NettyContainer {
        private static final Logger LOGGER = LoggerUtil.getLogger(NettyContainer.class);
        List<EventLoopGroup> eventLoopGroups = new ArrayList<>();
        int socketPort;
        List<RestartObserver> restartObservers;
        Map<String, Ctx> logs = new HashMap<>();
        Thread runThread;
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        public NettyContainer(int socketPort, List<RestartObserver> restartObservers) {
            this.socketPort = socketPort;
            this.restartObservers = restartObservers;
        }

        public void startSocket() {
            synchronized (eventLoopGroups) {
                if (runThread == null) {
                    runThread = new Thread(() -> {
                        EventLoopGroup bossGroup = new NioEventLoopGroup();
                        EventLoopGroup workerGroup = new NioEventLoopGroup();
                        this.eventLoopGroups.add(bossGroup);
                        this.eventLoopGroups.add(workerGroup);
                        try {
                            ServerBootstrap bootstrap = new ServerBootstrap();
                            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                                    .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    NettyProtocol.addHandler(socketChannel);
                                    socketChannel.pipeline().addLast(new LoggerHandler());
                                }
                            })
                                    .option(ChannelOption.SO_BACKLOG, 32 * 1024)//设置TCP缓冲区
                                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)//设置接受数据缓冲大小
                                    .option(ChannelOption.SO_SNDBUF, 32 * 1024)//设置发送数据缓冲大小
                                    .option(ChannelOption.SO_KEEPALIVE, true); //保持连接
                            ChannelFuture future = bootstrap.bind(socketPort).sync();
                            LOGGER.info("start socket for log. port:" + socketPort);
                            scheduledExecutorService.scheduleAtFixedRate(() -> {
                                LOGGER.info("------begin clear socket------");
                                Iterator<Map.Entry<String, Ctx>> iterator = logs.entrySet().iterator();
                                while (iterator.hasNext()) {
                                    Map.Entry<String, Ctx> next = iterator.next();
                                    if (next.getValue().canStopViewLog()) {
                                        LOGGER.info("------clear socket {} {} {} ------", next.getValue().clientIP, next.getValue().port, next.getValue().start);
                                        next.getValue().ctx.writeAndFlush(Unpooled.copiedBuffer(REQUEST_END.getBytes()));
                                        iterator.remove();
                                    }
                                }
                                if (Global.ISDEBUG) {
                                    LOGGER.info(logs.toString());
                                }
                            }, 0, 10000, TimeUnit.MILLISECONDS);
                            future.channel().closeFuture().sync();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            bossGroup.shutdownGracefully();
                            workerGroup.shutdownGracefully();
                        }
                    });
                    runThread.start();
                }
            }
        }

        public void stopSocket() {
            LOGGER.info("---------STOP ALL NettyContainer THREAD---------");
            this.eventLoopGroups.forEach(eventExecutors -> eventExecutors.shutdownGracefully());
            this.scheduledExecutorService.shutdownNow();
        }

        public void addCtx(ChannelHandlerContext ctx) {
            String id = Ctx.getId(ctx);
            if (!logs.containsKey(id)) {
                logs.put(id, new Ctx(ctx));
            }
        }

        public static class Ctx {
            ChannelHandlerContext ctx;
            String clientIP;
            int port;
            long start;

            public Ctx(ChannelHandlerContext ctx) {
                this.ctx = ctx;
                InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
                this.clientIP = insocket.getAddress().getHostAddress();
                this.port = insocket.getPort();
                this.start = System.currentTimeMillis();
            }

            public static String getId(ChannelHandlerContext ctx) {
                InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
                String clientIP = insocket.getAddress().getHostAddress();
                int port = insocket.getPort();
                String id = clientIP + "|" + port;
                return id;
            }

            public boolean canStopViewLog() {
                return System.currentTimeMillis() - start > 60000;
            }

            @Override
            public String toString() {
                return "Ctx{" +
                        "clientIP='" + clientIP + '\'' +
                        ", port=" + port +
                        ", start=" + start +
                        '}';
            }
        }

        class LoggerHandler extends ProtocolHandler<NettyProtocol> {

            @Override
            public void channelReadInternal(NettyProtocol read) throws Exception {
                restartObservers.forEach(restartObserver -> restartObserver.update(null, Ctx.getId(getCtx()) + "|" + read.stringVale()));
                write("ok.");
                addCtx(getCtx());
            }

        }
    }

}
