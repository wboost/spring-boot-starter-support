package top.wboost.config.client.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import top.wboost.common.base.enums.CharsetEnum;
import top.wboost.common.log.util.LoggerUtil;
import top.wboost.common.netty.handler.ProtocolHandler;
import top.wboost.common.netty.protocol.NettyProtocol;
import top.wboost.common.util.LoggerViewer;
import top.wboost.common.util.StringUtil;
import top.wboost.common.utils.web.utils.DateUtil;
import top.wboost.common.utils.web.utils.PropertiesUtil;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static top.wboost.config.client.core.ConfigClientRegister.REQUEST_END;

/**
 * @Auther: jwsun
 * @Date: 2019/1/8 21:46
 */
public class LoggerSender implements ApplicationListener<ContextClosedEvent> {

    Queue<SendLogMessage> queue = new ConcurrentLinkedQueue<>();
    private Logger logger = LoggerUtil.getLogger(LoggerSender.class);
    private EventLoopGroup workerGroup;
    private ChannelFuture clientFuture;
    private String applicationName;
    private String logPath;
    private String sk_ip;
    private Integer sk_port;
    private Integer sk_pid;
    private Long timestamp;
    private Long beginSendTimestamp;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private boolean sendStatus;
    private boolean sendBegin;
    private LoggerViewer loggerViewer;

    public LoggerSender() {
        try {
            this.applicationName = PropertiesUtil.getProperty("spring.application.name");
            this.sk_ip = PropertiesUtil.getProperty("sk_ip");
            this.sk_port = Integer.parseInt(PropertiesUtil.getPropertyOrDefault("sk_port", "0"));
            this.sk_pid = Integer.parseInt(PropertiesUtil.getPropertyOrDefault("sk_pid", "0"));
            this.timestamp = Long.parseLong(PropertiesUtil.getPropertyOrDefault("sk_ts", "0"));
            this.logPath = getLogPath();
            this.sendStatus = true;
            logger.info(String.format("true. socket for log.sk_ip:%s,sk_port:%s.sk_ts:%s", sk_ip, sk_port, timestamp));
        } catch (Exception e) {
            this.sendStatus = false;
        }
    }

    protected String getLogPath() {
        String filePath = ConfigClientApplicationListener.class.getResource("/").getPath().split("!")[0].split(":")[1];
        return filePath + ".restart." + DateUtil.format(new Date(timestamp), "yyyy-MM-dd-HH-mm-ss") + ".log";
    }

    protected void stopAll() {
        logger.info("--------------stopAll----------------");
        scheduledExecutorService.shutdown();
        loggerViewer.stop();
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
        }
    }

    public boolean canSend() {
        return this.sendStatus && StringUtil.notEmpty(sk_ip) && sk_port != 0 && timestamp != 0 && sk_pid != 0;
    }

    public void close() {
        stopAll();
    }

    @PostConstruct
    public void startSender() {
        start();
    }

    public boolean start() {
        if (canSend()) {
            // kill last pid
            String killCmd = "kill -9 " + sk_pid;
            logger.info("exec  cmd is " + killCmd);
            try {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", killCmd});
                logger.info("kill process {}", sk_pid);
            } catch (IOException e) {
                logger.error("kill process {} error.", e);
            }
            beginSendTimestamp = System.currentTimeMillis();
            Thread thread = new Thread(() -> {
                logger.info("socket for log.sk_ip:{},sk_port:{}.sk_ts:{}", sk_ip, sk_port, timestamp);
                this.workerGroup = new NioEventLoopGroup();
                try {
                    this.loggerViewer = getRestartLogger((ob, msg) -> {
                        SendLogMessage sendLogMessage = new SendLogMessage();
                        sendLogMessage.setAppName(this.applicationName);
                        sendLogMessage.setMsg(msg.toString());
                        queue.offer(sendLogMessage);
                    }, this.logPath);
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(workerGroup).handler(new LoggingHandler(io.netty.handler.logging.LogLevel.INFO)).channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    NettyProtocol.addHandler(socketChannel);
                                    socketChannel.pipeline().addLast(new LoggerHandler());
                                }
                            });
                    this.clientFuture = bootstrap.connect(new InetSocketAddress(sk_ip, sk_port)).sync();
                    scheduledExecutorService.scheduleAtFixedRate(() -> {
                        if (System.currentTimeMillis() - beginSendTimestamp > 60000) {
                            onApplicationEvent(null);
                            return;
                        }
                        StringBuffer stringBuffer = new StringBuffer();
                        if (!queue.isEmpty()) {
                            while (!queue.isEmpty()) {
                                stringBuffer.append(queue.poll() + "\n");
                            }
                            this.clientFuture.channel().writeAndFlush(new NettyProtocol(stringBuffer.toString().getBytes(CharsetEnum.UTF_8.getCharset())));
                        }
                    }, 0, 1500, TimeUnit.MILLISECONDS);
                    this.clientFuture.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.setDaemon(true);
            thread.start();
            sendBegin = true;
            return true;
        }

        return false;
    }

    protected LoggerViewer getRestartLogger(Observer observer, String logPath) throws Exception {
        LoggerViewer loggerViewer = new LoggerViewer(observer);
        loggerViewer.realtimeShowLog(new File(logPath));
        return loggerViewer;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (sendBegin) {
            logger.info("close loggerSender." );
            close();
        }
    }

    class LoggerHandler extends ProtocolHandler<NettyProtocol> {

        @Override
        public void channelReadInternal(NettyProtocol read) throws Exception {
            if (logger.isDebugEnabled())
                logger.debug("config server receive: " + read.stringVale());
            if (REQUEST_END.equals(read.stringVale())) {
                logger.info("logSender end! stop socket.");
                // 关闭所有
                close();
            }
        }
    }
}
