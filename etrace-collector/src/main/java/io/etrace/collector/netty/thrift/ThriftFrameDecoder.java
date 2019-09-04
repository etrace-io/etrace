package io.etrace.collector.netty.thrift;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TTransport;

import java.util.List;

public class ThriftFrameDecoder extends ByteToMessageDecoder {
    public static final int MESSAGE_FRAME_SIZE = 4;
    private final int maxFrameSize;
    private final TProtocolFactory inputProtocolFactory;
    //private final Counter unframedMessageCounter;

    public ThriftFrameDecoder(int maxFrameSize, TProtocolFactory inputProtocolFactory) {
        this.maxFrameSize = maxFrameSize;
        this.inputProtocolFactory = inputProtocolFactory;
        //this.unframedMessageCounter = MetricsService.getInstance().counter(new MetricName("unframed.message"));

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (!buffer.isReadable()) {
            return;
        }
        TTransport ttransport = null;
        short firstByte = buffer.getUnsignedByte(0);
        if (firstByte >= 0x80) {
            // A non-zero MSB for the first byte of the message implies the message starts with a
            // protocol id (and thus it is unframed).

            //todo: 此处改成 micrometer
            //unframedMessageCounter.inc();

            ttransport = tryDecodeUnframedMessage(ctx, buffer);
        } else if (buffer.readableBytes() >= MESSAGE_FRAME_SIZE) {
            // Messages with a zero MSB in the first byte are framed messages
            ttransport = tryDecodeFramedMessage(ctx, buffer);
        }
        if (ttransport != null) {
            out.add(ttransport);
        }
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

    private TTransport tryDecodeFramedMessage(ChannelHandlerContext ctx, ByteBuf buffer) {
        // Framed messages are prefixed by the size of the frame (which doesn't include the
        // framing itself).

        int messageStartReaderIndex = buffer.readerIndex();
        // Read the i32 frame contents size
        int messageContentsLength = buffer.getInt(messageStartReaderIndex);
        // The full message is larger by the size of the frame size prefix
        int messageLength = messageContentsLength + MESSAGE_FRAME_SIZE;

        if (messageContentsLength > maxFrameSize) {
            throw new TooLongFrameException("Maximum frame size of " + maxFrameSize + " exceeded");
        }

        int messageContentsOffset = messageStartReaderIndex + MESSAGE_FRAME_SIZE;
        if (messageLength == 0) {
            // Zero-sized frame: just ignore it and return nothing
            buffer.readerIndex(messageContentsOffset);
            return null;
        } else if (buffer.readableBytes() < messageLength) {
            // Full message isn't available yet, return nothing for now
            return null;
        } else {
            // Full message is available, return it
            ByteBuf messageBuffer = extractFrame(buffer, messageContentsOffset, messageContentsLength);
            ThriftMessage message = new ThriftMessage(messageBuffer, ThriftTransportType.FRAMED);
            buffer.readerIndex(messageStartReaderIndex + messageLength);
            return new TNettyTransport(ctx.channel(), message);
        }
    }

    private TTransport tryDecodeUnframedMessage(ChannelHandlerContext ctx, ByteBuf buffer) throws TException {
        // Perform a trial decode, skipping through
        // the fields, to see whether we have an entire message available.

        int messageLength = 0;
        int messageStartReaderIndex = buffer.readerIndex();

        TNettyTransport decodeAttemptTransport = new TNettyTransport(ctx.channel(), buffer);
        try {
            TProtocol inputProtocol = this.inputProtocolFactory.getProtocol(decodeAttemptTransport);

            // Skip through the message
            inputProtocol.readMessageBegin();
            TProtocolUtil.skip(inputProtocol, TType.STRUCT);
            inputProtocol.readMessageEnd();

            messageLength = buffer.readerIndex() - messageStartReaderIndex;
        } catch (IndexOutOfBoundsException e) {
            // No complete message was decoded: ran out of bytes
            return null;
        } finally {
            decodeAttemptTransport.release();
            if (buffer.readerIndex() - messageStartReaderIndex > maxFrameSize) {
                throw new TooLongFrameException("Maximum frame size of " + maxFrameSize + " exceeded");
            }
            buffer.readerIndex(messageStartReaderIndex);
        }

        if (messageLength <= 0) {
            return null;
        }

        // We have a full message in the read buffer, slice it off
        ByteBuf messageBuffer = extractFrame(buffer, messageStartReaderIndex, messageLength);
        ThriftMessage message = new ThriftMessage(messageBuffer, ThriftTransportType.UNFRAMED);
        buffer.readerIndex(messageStartReaderIndex + messageLength);
        return new TNettyTransport(ctx.channel(), message);
    }

    protected ByteBuf extractFrame(ByteBuf buffer, int index, int length) {
        // Slice should be sufficient here (and avoids the copy in LengthFieldBasedFrameDecoder)
        // because we know no one is going to modify the contents in the read buffers.
        return buffer.slice(index, length);
        //        return buffer.copy(index, length);
    }

}
