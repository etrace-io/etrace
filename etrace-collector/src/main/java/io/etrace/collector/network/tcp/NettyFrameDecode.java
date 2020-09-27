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

package io.etrace.collector.network.tcp;

import io.etrace.common.util.Pair;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.serialization.ObjectDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;

// todo: 比较与 原版的区别
public class NettyFrameDecode extends ByteToMessageDecoder {

    private final static Logger LOGGER = LoggerFactory.getLogger(NettyFrameDecode.class);

    private final ByteOrder byteOrder;
    private final int maxFrameLength;
    private final int fieldLength;
    private final boolean failFast;
    private boolean discardingTooLongFrame;
    private long tooLongFrameLength;
    private long bytesToDiscard;

    public NettyFrameDecode(int maxFrameLength, int lengthFieldLength) {
        this(ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldLength, true);
    }

    public NettyFrameDecode(ByteOrder byteOrder, int maxFrameLength, int lengthFieldLength, boolean failFast) {
        if (byteOrder == null) {
            throw new NullPointerException("byteOrder");
        }

        if (maxFrameLength <= 0) {
            throw new IllegalArgumentException("maxFrameLength must be a positive integer: " + maxFrameLength);
        }
        this.byteOrder = byteOrder;
        this.maxFrameLength = maxFrameLength;
        this.fieldLength = lengthFieldLength;
        this.failFast = failFast;
    }

    /**
     * @param ctx ctx
     * @param in  在
     * @param out 出
     * @throws Exception 异常
     */
    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable()) {
            return;
        }

        ByteBuf decoded = decode(ctx, in);
        if (decoded != null) {
            Pair<byte[], byte[]> pair = getMessageStruct(decoded);
            if (null != pair) {
                out.add(pair);
            }
        }
    }

    /**
     * @param buffer 缓冲
     * @return {@link Pair}
     */
    private Pair<byte[], byte[]> getMessageStruct(ByteBuf buffer) {
        Pair<byte[], byte[]> pair = null;
        try {
            if (buffer.readableBytes() > 8) {
                //header
                int headerLength = buffer.readInt();
                if (buffer.readableBytes() > headerLength) {
                    byte[] header = new byte[headerLength];
                    buffer.readBytes(header);
                    //chunk
                    int bodyLength = buffer.readInt();
                    if (buffer.readableBytes() == bodyLength) {
                        byte[] body = new byte[bodyLength];
                        buffer.readBytes(body);
                        pair = new Pair<>();
                        pair.setKey(header);
                        pair.setValue(body);
                    }
                }
            } else {
                LOGGER.warn("Too few readable bytes!");
            }
        } catch (Exception e) {
            LOGGER.error("decode package error:", e);
            pair = null;
        } finally {
            //            buffer.release();
        }
        return pair;
    }

    /**
     * @param type      类型
     * @param in        在
     * @param tfe       tfe
     * @param readIndex 阅读指数
     * @param ctx       ctx
     * @throws IOException IOException
     */
    private void traceException(String type, ByteBuf in, TooLongFrameException tfe, Integer readIndex,
                                ChannelHandlerContext ctx)
        throws IOException {
        try {
            if (null != readIndex) {
                in.readerIndex(readIndex);
                if (in.readableBytes() > 8) {
                    long frameSize = in.getUnsignedInt(readIndex);
                    LOGGER.error("TooLongFrameException[{}]:", frameSize, tfe);
                    //                    int header_length = (int) in.getUnsignedInt(readIndex + 4);
                    //                    byte[] header = new byte[header_length];
                    //                    if (in.readableBytes() > header_length) {
                    //                        Map<String, String> tags = newHashMap();
                    //                        in.getBytes(readIndex + 8, header);
                    //                        MessageHeader messageHeader = JSONUtil.toObject(header, MessageHeader
                    //                        .class);
                    //                        tags.put("packageSize", String.valueOf(frameSize));
                    //                        tags.put("messageHeader", new String(header));
                    //                        tags.put("exception", message);
                    //                        Trace.logEvent(type, messageHeader.getAppId(), "success", tags);
                    //                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("LengthFieldFrameDecoder:", e);
        } finally {
            in.skipBytes(in.readableBytes());
            ctx.close();
        }
    }

    /**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in  the {@link ByteBuf} from which to read data
     * @return frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could be
     * created.
     * @throws Exception Exception
     */
    protected ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (discardingTooLongFrame) {
            long bytesToDiscard = this.bytesToDiscard;
            int localBytesToDiscard = (int)Math.min(bytesToDiscard, in.readableBytes());
            in.skipBytes(localBytesToDiscard);
            bytesToDiscard -= localBytesToDiscard;
            this.bytesToDiscard = bytesToDiscard;

            failIfNecessary(false);
        }

        if (in.readableBytes() < fieldLength) {
            return null;
        }

        int actualLengthFieldOffset = in.readerIndex();
        long frameLength = getUnadjustedFrameLength(in, actualLengthFieldOffset, byteOrder);

        frameLength += fieldLength;
        if (frameLength < fieldLength) {
            in.skipBytes(fieldLength);
            throw new CorruptedFrameException(
                "Adjusted frame length (" + frameLength + ") is less " +
                    "than lengthFieldEndOffset: " + fieldLength);
        }

        if (frameLength > maxFrameLength) {
            long discard = frameLength - in.readableBytes();
            tooLongFrameLength = frameLength;
            Integer readIndex = null;
            if (discard < 0) {
                // buffer contains more bytes then the frameLength so we can discard all now
                in.skipBytes((int)frameLength);
            } else {
                // Enter the discard mode and discard everything received so far.
                discardingTooLongFrame = true;
                bytesToDiscard = discard;
                readIndex = in.readerIndex();
                in.skipBytes(in.readableBytes());
            }
            try {
                failIfNecessary(true);
            } catch (TooLongFrameException e) {
                traceException("FrameDecode-TooLongException", in, e, readIndex, ctx);
            }
            return null;
        }

        // never overflows because it's less than maxFrameLength
        int frameLengthInt = (int)frameLength;
        if (in.readableBytes() < frameLengthInt) {
            return null;
        }

        in.skipBytes(fieldLength);

        // extract frame
        int readerIndex = in.readerIndex(); // readerIndex = 4
        int actualFrameLength = frameLengthInt - fieldLength; //actualFrameLength = data_length
        //        ByteBuf frame = extractFrame(ctx, in, readerIndex, actualFrameLength);
        ByteBuf frame = in.slice(readerIndex, actualFrameLength);
        //        frame.retain();
        in.readerIndex(actualFrameLength + readerIndex);//frameLengthInt
        return frame;
    }

    /**
     * Decodes the specified region of the buffer into an unadjusted frame length.  The default implementation is
     * capable of decoding the specified region into an unsigned 8/16/24/32/64 bit integer.  Override this method to
     * decode the length field encoded differently.  Note that this method must not modify the state of the specified
     * buffer (e.g. {@code readerIndex}, {@code writerIndex}, and the content of the buffer.)
     *
     * @param buf    buf
     * @param offset offset
     * @param order  order
     * @return long long
     * @throws DecoderException if failed to decode the specified region
     */
    protected long getUnadjustedFrameLength(ByteBuf buf, int offset, ByteOrder order) {
        buf = buf.order(order);
        return buf.getUnsignedInt(offset);
    }

    private void failIfNecessary(boolean firstDetectionOfTooLongFrame) {
        if (bytesToDiscard == 0) {
            // Reset to the initial state and tell the handlers that
            // the frame was too large.
            long tooLongFrameLength = this.tooLongFrameLength;
            this.tooLongFrameLength = 0;
            discardingTooLongFrame = false;
            if (!failFast ||
                failFast && firstDetectionOfTooLongFrame) {
                fail(tooLongFrameLength);
            }
        } else {
            // Keep discarding and notify handlers if necessary.
            if (failFast && firstDetectionOfTooLongFrame) {
                fail(tooLongFrameLength);
            }
        }
    }

    /**
     * 提取帧 Extract the sub-region of the specified buffer.
     * <p>
     * If you are sure that the frame and its content are not accessed after the current {@link
     * #decode(ChannelHandlerContext, ByteBuf)} call returns, you can even avoid memory copy by returning the sliced
     * sub-region (i.e. <tt>return buffer.slice(index, length)</tt>). It's often useful when you convert the extracted
     * frame into an object. Refer to the source code of {@link ObjectDecoder} to see how this method is overridden to
     * avoid memory copy.
     *
     * @param ctx    ctx
     * @param buffer 缓冲
     * @param index  指数
     * @param length 长度
     * @return {@link ByteBuf}
     */
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        ByteBuf frame = ctx.alloc().buffer(length);
        frame.writeBytes(buffer, index, length);
        return frame;
    }

    private void fail(long frameLength) {
        if (frameLength > 0) {
            throw new TooLongFrameException(
                "Adjusted frame length exceeds " + maxFrameLength +
                    ": " + frameLength + " - discarded");
        } else {
            throw new TooLongFrameException(
                "Adjusted frame length exceeds " + maxFrameLength +
                    " - discarding");
        }
    }

    /**
     * 例外了
     *
     * @param ctx   ctx
     * @param cause 导致
     * @throws Exception 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //        Trace.logError(cause);
        LOGGER.error("message-decode-error:", cause);
        ctx.close();
    }

    /**
     * 频道不活跃
     *
     * @param ctx ctx
     * @throws Exception 异常
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        super.channelInactive(ctx);
    }
}
