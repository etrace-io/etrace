package io.etrace.collector.netty.tcp;

import com.google.common.base.Strings;
import io.etrace.collector.netty.DefaultHandler;
import io.etrace.common.modal.Pair;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MessageHandler extends SimpleChannelInboundHandler<Pair<byte[], byte[]>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    @Autowired
    private DefaultHandler handler;
    private ExecutorService executorService;
    private static final String serverType = "tcp";

    @PostConstruct
    public void postConstruct() {
        ExecutorService executorService = Executors.newFixedThreadPool(6, new ThreadFactory() {
            private AtomicInteger id = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Frame-handler-" + id.incrementAndGet());
            }
        });

        this.executorService = executorService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        tryResetConn(ctx);
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Pair<byte[], byte[]> pair)
        throws Exception {
        executorService.execute(() -> handler.process(pair.getKey(), pair.getValue(), serverType));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        super.channelInactive(ctx);
    }

    // need to refactor
    private void tryResetConn(ChannelHandlerContext ctx) {
        try {
            if (ctx.channel() == null) {
                return;
            }
            if (!(ctx.channel().remoteAddress() instanceof InetSocketAddress)) {
                return;
            }
            InetAddress address = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress();
            if (address == null) {
                return;
            }
            String host = address.getHostAddress();
            if (Strings.isNullOrEmpty(host)) {
                return;
            }
            //Set<String> set = shardIngConfig.getResetConnIp();
            //if (set.isEmpty()) {
            //    return;
            //}
            //if (set.contains(host)) {
            //    ctx.close();
            //    metricsService.emptyListAgent(host);
            //}
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
}
