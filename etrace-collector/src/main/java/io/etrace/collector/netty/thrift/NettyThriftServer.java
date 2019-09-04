package io.etrace.collector.netty.thrift;

import io.etrace.agent.Trace;
import io.etrace.collector.netty.AbstractServer;
import io.etrace.collector.netty.DefaultHandler;
import io.etrace.common.rpc.MessageService;
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
import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NettyThriftServer extends AbstractServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(NettyThriftServer.class);
    @Value("${network.thread.num}")
    private int threadNum;

    @Value("${network.epoll_lever}")
    boolean useLevelTriggered;

    @Value("${network.thrift.port}")
    private int thriftPort;

    @Autowired
    private DefaultHandler handler;
    @Autowired
    private  NettyDispatcher nettyDispatcher;


    private ChannelFuture future;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;

    protected boolean getOSMatches(String osNamePrefix) {
        String os = System.getProperty("os.name");
        return os != null && os.startsWith(osNamePrefix);
    }

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
                // Create the handler
                MessageService.Iface serviceInterface = new ThriftHandler(handler);
                // Create the processor
                TProcessor processor = new MessageService.Processor<>(serviceInterface);
                ThriftServerDef def = new ThriftServerDefBuilder().withProcessor(processor).build();

                nettyDispatcher.init(def);

                bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("frameDecoder",
                            new ThriftFrameDecoder(def.getMaxFrameSize(), def.getInProtocolFactory()));
                        pipeline.addLast("dispatcher", nettyDispatcher);

                    }
                });
                bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
                bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
                bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);

                if (useLevelTriggered) {
                    bootstrap.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
                    Trace.logEvent("Netty-EPOLL-MODE", useLevelTriggered + " " + EpollMode.LEVEL_TRIGGERED);
                }

                try {
                    LOGGER.info("Started NETTY transport server {}:{}, WorkerThread [{}]", def.getName(), thriftPort,
                        threadNum);
                    Trace.logEvent("NettyServer", "WorkerThread:" + threadNum);
                    future = bootstrap.bind(thriftPort).sync();
                    future.channel().closeFuture().sync().channel();
                } catch (Exception e) {
                    LOGGER.error("Started netty Server Failed:" + thriftPort, e);
                }

            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        });
        thread.start();
    }

    @Override
    public void shutdownServer() {
        try {
            System.out.println("Started shutdown netty transport server " + thriftPort);
            if (future != null) {
                future.channel().closeFuture();
            }
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("Shutdown netty thrift server success.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
