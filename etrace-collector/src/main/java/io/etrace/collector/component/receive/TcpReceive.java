package io.etrace.collector.component.receive;

import io.etrace.collector.network.tcp.NettyFrameDecode;
import io.etrace.collector.network.tcp.TcpRecvHandler;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TcpReceive extends DefaultSyncTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpReceive.class);

    private int port;
    private int workerNum;
    private int maxFrameSize;

    private ChannelFuture future;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;

    public TcpReceive(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        this.port = Integer.parseInt(params.get("port").toString());
        this.workerNum = (int)Optional.ofNullable(params.get("workers")).orElse(8);
        this.maxFrameSize = (int)Optional.ofNullable(params.get("maxFrameSize")).orElse(15 * 1024 * 1024);
    }

    @Override
    public void startup() {
        Thread thread = new Thread(() -> {
            boolean linux = getOSMatches("Linux") || getOSMatches("LINUX");
            bossGroup = linux ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
            workerGroup = linux ? new EpollEventLoopGroup(workerNum) : new NioEventLoopGroup(workerNum);

            ExecutorService es = new ThreadPoolExecutor(8, 8, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private AtomicInteger id = new AtomicInteger();

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "Tcp-Message-handler-" + id.incrementAndGet());
                    }
                });

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
                        pipeline.addLast("tcpFrameDecoder", new NettyFrameDecode(maxFrameSize, 4));
                        pipeline.addLast("tcpHandler", new TcpRecvHandler(es, component));

                    }
                });
                bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
                bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
                bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);

                try {
                    LOGGER.info("Started NETTY TCP server Netty-Tcp:{}, WorkerThread [{}]", port, workerNum);
                    future = bootstrap.bind(port).sync();
                    future.channel().closeFuture().sync().channel();
                } catch (Exception e) {
                    LOGGER.error("Started Tcp Server Failed:" + port, e);
                }
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        });
        thread.start();
    }

    boolean getOSMatches(String osNamePrefix) {
        String os = System.getProperty("os.name");
        return os != null && os.startsWith(osNamePrefix);
    }
}
