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

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.concurrent.Executor;

/**
 * Builder for the Thrift Server descriptor. Example :
 * <code>
 * new ThriftServerDefBuilder() .listen(conf.getServerPort()) .limitFrameSizeTo(conf.getMaxFrameSize())
 * .withProcessor(new FacebookService.Processor(new MyFacebookBase())) .using(Executors.newFixedThreadPool(5))
 * .build();
 * <p>
 * You can then pass ThriftServerDef to guice via a multibinder.
 * </code>
 */
public class ThriftServerDefBuilder {
    private int maxFrameSize;
    private TProcessorFactory processorFactory;
    private TProtocolFactory inProtocolFact;
    private Executor executor;
    private String name = "Netty-Thrift";

    /**
     * Create a ThriftServerDefBuilder with common defaults
     */
    public ThriftServerDefBuilder() {
        this.maxFrameSize = 16384000;
        this.inProtocolFact = new TBinaryProtocol.Factory();
        this.executor = Runnable::run;
    }

    /**
     * Give the endpoint a more meaningful name.
     *
     * @param name name
     * @return {@link ThriftServerDefBuilder}
     */
    public ThriftServerDefBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Specify protocolFactory for both input and output
     *
     * @param tProtocolFactory tProtocolFactory
     * @return {@link ThriftServerDefBuilder}
     */
    public ThriftServerDefBuilder speaks(TProtocolFactory tProtocolFactory) {
        this.inProtocolFact = tProtocolFactory;
        return this;
    }

    /**
     * Specify the TProcessor.
     *
     * @param p p
     * @return {@link ThriftServerDefBuilder}
     */
    public ThriftServerDefBuilder withProcessor(TProcessor p) {
        this.processorFactory = new TProcessorFactory(p);
        return this;
    }

    /**
     * 帧大小限制 Set frame size limit.  Default is 1M
     *
     * @param maxFrameSize 最大帧大小
     * @return {@link ThriftServerDefBuilder}
     */
    public ThriftServerDefBuilder limitFrameSizeTo(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
        return this;
    }

    /**
     * 与处理器工厂 Anohter way to specify the TProcessor.
     *
     * @param processorFactory 处理器工厂
     * @return {@link ThriftServerDefBuilder}
     */
    public ThriftServerDefBuilder withProcessorFactory(TProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
        return this;
    }

    /**
     * 在协议 Specify only the input protocol.
     *
     * @param tProtocolFactory t协议工厂
     * @return {@link ThriftServerDefBuilder}
     */
    public ThriftServerDefBuilder inProtocol(TProtocolFactory tProtocolFactory) {
        this.inProtocolFact = tProtocolFactory;
        return this;
    }

    /**
     * 使用 Specify an executor for thrift processor invocations ( i.e. = THaHsServer ) By default invocation happens in
     * Netty single thread ( i.e. = TNonBlockingServer )
     *
     * @param exe exe
     * @return {@link ThriftServerDefBuilder}
     */
    public ThriftServerDefBuilder using(Executor exe) {
        this.executor = exe;
        return this;
    }

    /**
     * Build the ThriftServerDef
     *
     * @return {@link ThriftServerDef}
     */
    public ThriftServerDef build() {
        if (processorFactory == null) {
            throw new IllegalStateException("processor not defined !");
        }
        return new ThriftServerDef(name, maxFrameSize, processorFactory, inProtocolFact, executor);
    }
}
