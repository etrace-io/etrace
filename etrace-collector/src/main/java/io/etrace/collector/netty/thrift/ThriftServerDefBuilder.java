package io.etrace.collector.netty.thrift;

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.concurrent.Executor;

/**
 * Builder for the Thrift Server descriptor. Example :
 * <code>
 * new ThriftServerDefBuilder()
 * .listen(config.getServerPort())
 * .limitFrameSizeTo(config.getMaxFrameSize())
 * .withProcessor(new FacebookService.Processor(new MyFacebookBase()))
 * .using(Executors.newFixedThreadPool(5))
 * .build();
 * <p>
 * <p>
 * You can then pass ThriftServerDef to guice via a multibinder.
 * <p>
 * </code>
 */
public class ThriftServerDefBuilder {
    private int serverPort;
    private int maxFrameSize;
    private TProcessorFactory processorFactory;
    private TProtocolFactory inProtocolFact;
    private Executor executor;
    private String name = "Netty-Thrift";

    /**
     * Create a ThriftServerDefBuilder with common defaults
     */
    public ThriftServerDefBuilder() {
        this.serverPort = 8080;
        this.maxFrameSize = 16384000;
        this.inProtocolFact = new TBinaryProtocol.Factory();
        this.executor = runnable -> runnable.run();
    }

    /**
     * Give the endpoint a more meaningful name.
     */
    public ThriftServerDefBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Listen to this port.
     */
    public ThriftServerDefBuilder listen(int serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    /**
     * Specify protocolFactory for both input and output
     */
    public ThriftServerDefBuilder speaks(TProtocolFactory tProtocolFactory) {
        this.inProtocolFact = tProtocolFactory;
        return this;
    }

    /**
     * Specify the TProcessor.
     */
    public ThriftServerDefBuilder withProcessor(TProcessor p) {
        this.processorFactory = new TProcessorFactory(p);
        return this;
    }

    /**
     * Set frame size limit.  Default is 1M
     */
    public ThriftServerDefBuilder limitFrameSizeTo(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
        return this;
    }

    /**
     * Anohter way to specify the TProcessor.
     */
    public ThriftServerDefBuilder withProcessorFactory(TProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
        return this;
    }

    /**
     * Specify only the input protocol.
     */
    public ThriftServerDefBuilder inProtocol(TProtocolFactory tProtocolFactory) {
        this.inProtocolFact = tProtocolFactory;
        return this;
    }

    /**
     * Specify an executor for thrift processor invocations ( i.e. = THaHsServer )
     * By default invocation happens in Netty single thread
     * ( i.e. = TNonBlockingServer )
     */
    public ThriftServerDefBuilder using(Executor exe) {
        this.executor = exe;
        return this;
    }

    /**
     * Build the ThriftServerDef
     */
    public ThriftServerDef build() {
        if (processorFactory == null) {
            throw new IllegalStateException("processor not defined !");
        }
        return new ThriftServerDef(name, serverPort, maxFrameSize, processorFactory, inProtocolFact, executor);
    }
}
