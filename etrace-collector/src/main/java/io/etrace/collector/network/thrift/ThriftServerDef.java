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

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.concurrent.Executor;

/**
 * Descriptor for a Thrift Server. This defines a listener port that Nifty need to start a Thrift endpoint.
 */
public class ThriftServerDef {
    private final int maxFrameSize;
    private final TProcessorFactory processorFactory;
    private final TProtocolFactory inProtocolFact;

    private final Executor executor;
    private final String name;

    public ThriftServerDef(String name, int maxFrameSize, TProcessorFactory factory,
                           TProtocolFactory inProtocolFact, Executor executor) {
        this.name = name;
        this.maxFrameSize = maxFrameSize;
        this.processorFactory = factory;
        this.inProtocolFact = inProtocolFact;
        this.executor = executor;
    }

    public static ThriftServerDefBuilder newBuilder() {
        return new ThriftServerDefBuilder();
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public TProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    public TProtocolFactory getInProtocolFactory() {
        return inProtocolFact;
    }

    public Executor getExecutor() {
        return executor;
    }

    public String getName() {
        return name;
    }
}
