package io.etrace.collector.netty.thrift;

import io.netty.buffer.ByteBuf;

public class ThriftMessage {
	private final ByteBuf buffer;
	private final ThriftTransportType transportType;

	public ThriftMessage(ByteBuf buffer, ThriftTransportType transportType) {
		this.buffer = buffer;
		this.transportType = transportType;
	}

	public ByteBuf getBuffer() {
		return buffer;
	}

	public ThriftTransportType getTransportType() {
		return transportType;
	}
}
