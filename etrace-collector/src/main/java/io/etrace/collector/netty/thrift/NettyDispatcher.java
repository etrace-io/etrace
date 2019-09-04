package io.etrace.collector.netty.thrift;

import com.google.common.base.Strings;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

@Component
public class NettyDispatcher extends SimpleChannelInboundHandler<TNettyTransport> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyDispatcher.class);

    private TProcessorFactory processorFactory;
    private TProtocolFactory inProtocolFactory;
    private Executor executor;

    public void init(ThriftServerDef def) {
        this.processorFactory = def.getProcessorFactory();
        this.inProtocolFactory = def.getInProtocolFactory();
        this.executor = def.getExecutor();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        tryResetConn(ctx);
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TNettyTransport msg) throws Exception {
        processRequest(ctx, msg);
    }

    private void processRequest(final ChannelHandlerContext ctx, final TNettyTransport messageTransport) {
        executor.execute(() -> {
            TProtocol inProtocol = inProtocolFactory.getProtocol(messageTransport);
            try {
                processorFactory.getProcessor(messageTransport).process(inProtocol, null);
            } catch (Throwable e1) {
                LOGGER.error("Exception while invoking!", e1);
                closeChannel(ctx);
            } finally {
                messageTransport.release();
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Any out of band exception are caught here and we tear down the socket
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        if (ctx.channel().isOpen()) {
            ctx.channel().close();
        }
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

            // todo: use micrometer
            //metricsService.emptyListAgent(host);

            //}
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
}