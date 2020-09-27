/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.collector.network.thrift;

import com.google.common.base.Strings;
import io.etrace.collector.service.CollectorConfigurationService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

@org.springframework.stereotype.Component

//io.netty.channel.ChannelPipelineException: io.etrace.collector.network.thrift.ThriftHandler is not a @Sharable
// handler, so can't be added or removed multiple times.
@ChannelHandler.Sharable
public class ThriftHandler extends SimpleChannelInboundHandler<TNettyTransport> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftHandler.class);

    private TProcessorFactory processorFactory;
    private TProtocolFactory inProtocolFactory;
    private Executor executor;
    @Autowired
    private CollectorConfigurationService collectorConfigurationService;

    public void setThriftServerDef(ThriftServerDef def) {
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

            if (collectorConfigurationService.isResetConnIp(host)) {
                ctx.close();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
}
