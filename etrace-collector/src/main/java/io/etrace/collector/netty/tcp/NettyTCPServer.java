package io.etrace.collector.netty.tcp;

import io.etrace.agent.Trace;
import io.etrace.collector.netty.AbstractServer;
import io.etrace.collector.netty.DefaultHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class NettyTCPServer extends AbstractServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(NettyTCPServer.class);

    @Value("${network.tcp.thread.num}")
    private int threadNum;

    @Value("${network.epoll_lever}")
    boolean useLevelTriggered;

    @Value("${network.tcp.port}")
    private int tcpPort;
    @Value("${network.max.frame.size}")
    private int maxFrameSize;

    private DefaultHandler handler;
    private ChannelFuture future;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;

    @Autowired
    private MessageHandler messageHandler;

    private boolean getOSMatches(String osNamePrefix) {
        String os = System.getProperty("os.name");
        return os != null && os.startsWith(osNamePrefix);
    }

    @PostConstruct
    @Override
    public void startup() {
        Thread thread = new Thread(() -> {
            boolean linux = getOSMatches("Linux") || getOSMatches("LINUX");
            bossGroup = linux ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
            workerGroup = linux ? new EpollEventLoopGroup(threadNum) : new NioEventLoopGroup(threadNum);

            try {
                bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup);
                if (linux) {
                    bootstrap.channel(EpollServerSocketChannel.class);
                } else {
                    bootstrap.channel(NioServerSocketChannel.class);
                }

                bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("frameDecoder", new NettyFrameDecode(maxFrameSize, 4));
                        pipeline.addLast("dispatcher", messageHandler);

                    }
                });
                bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
                bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
                bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);

                if (useLevelTriggered) {
                    bootstrap.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
                    Trace.logEvent("Netty-Tcp-EPOLL-MODE", useLevelTriggered + " " + EpollMode.LEVEL_TRIGGERED);
                }
                try {
                    LOGGER.info("Started NETTY TCP server Netty-Tcp:{}, WorkerThread [{}]", tcpPort, threadNum);
                    Trace.logEvent("TcpServer", "WorkerThread:" + threadNum);
                    future = bootstrap.bind(tcpPort).sync();
                    future.channel().closeFuture().sync().channel();
                    System.out.println("tcp server start");
                } catch (Exception e) {
                    LOGGER.error("Started Tcp Server Failed:" + tcpPort, e);
                }

            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        });
        thread.start();
    }

    @PreDestroy
    @Override
    public void shutdownServer() {
        try {
            System.out.println("Started shutdown netty tcp server " + tcpPort);
            if (future != null) {
                future.channel().closeFuture();
            }
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("Shutdown netty tcp server success.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
