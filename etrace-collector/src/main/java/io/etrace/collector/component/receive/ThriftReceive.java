package io.etrace.collector.component.receive;

import io.etrace.collector.network.thrift.ThriftFrameDecoder;
import io.etrace.collector.network.thrift.ThriftHandler;
import io.etrace.collector.network.thrift.ThriftServerDef;
import io.etrace.collector.network.thrift.ThriftServerDefBuilder;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.etrace.common.thrift.MessageService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ThriftReceive extends DefaultSyncTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftReceive.class);
    @Autowired
    ThriftHandler thriftHandler;
    private int port;
    private int workerNum;
    private ChannelFuture future;
    private EventLoopGroup parentGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;

    public ThriftReceive(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
        this.port = Integer.parseInt(params.get("port").toString());
        this.workerNum = (int)Optional.ofNullable(params.get("workers")).orElse(8);
    }

    @PostConstruct
    @Override
    public void startup() {
        super.startup();

        Thread thread = new Thread(() -> {
            boolean linux = getOSMatches("Linux") || getOSMatches("LINUX");
            parentGroup = linux ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
            workerGroup = linux ? new EpollEventLoopGroup(workerNum) : new NioEventLoopGroup(workerNum);
            try {
                bootstrap = new ServerBootstrap();
                bootstrap.group(parentGroup, workerGroup);
                if (linux) {
                    bootstrap.channel(EpollServerSocketChannel.class);
                } else {
                    bootstrap.channel(NioServerSocketChannel.class);
                }
                // Create the processor
                TProcessor processor = new MessageService.Processor<>(
                    (head, message) -> component.dispatch(head.array(), message.array()));
                ThriftServerDef def = new ThriftServerDefBuilder().withProcessor(processor).build();
                thriftHandler.setThriftServerDef(def);

                bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("thriftFrameDecoder",
                            new ThriftFrameDecoder(def.getMaxFrameSize(), def.getInProtocolFactory()));
                        pipeline.addLast("thriftHandler", thriftHandler);
                    }
                });

                bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
                bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
                bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);

                try {
                    LOGGER.info("Started NETTY transport server {}:{}, WorkerThread [{}]", def.getName(), port,
                        workerNum);
                    future = bootstrap.bind(port).sync();
                    future.channel().closeFuture().sync().channel();
                } catch (Exception e) {
                    LOGGER.error("Started netty Server Failed:" + port, e);
                }

            } finally {
                workerGroup.shutdownGracefully();
                parentGroup.shutdownGracefully();
            }
        });
        thread.start();

    }

    @PreDestroy
    @Override
    public void stop() {
        super.stop();
        System.out.println("Started shutdown netty transport server " + port);
        if (future != null) {
            future.channel().closeFuture();
        }
        parentGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        System.out.println("Shutdown netty thrift server success.");
    }

    boolean getOSMatches(String osNamePrefix) {
        String os = System.getProperty("os.name");
        return os != null && os.startsWith(osNamePrefix);
    }

}
