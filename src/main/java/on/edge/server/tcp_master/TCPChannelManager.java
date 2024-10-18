package on.edge.server.tcp_master;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

/**
 * 编解码器
 */
@SuppressWarnings("all")
public abstract class TCPChannelManager {

    private ByteToMessageDecoder decoder;

    /**
     * 基于分隔符处理数据帧
     * @param separator 分隔符
     * @param maxFrameLength 报文最大长度
     */
    public final TCPChannelManager buildSeparator(String separator, int maxFrameLength) {
        ByteBuf buf = Unpooled.copiedBuffer(separator.getBytes());
        this.decoder = new DelimiterBasedFrameDecoder(maxFrameLength, buf);
        return this;
    }


    /**
     * 基于报文长度处理数据帧
     * @param length 报文长度
     */
    public final TCPChannelManager buildFixedLength(int length) {
        this.decoder = new FixedLengthFrameDecoder(length);
        return this;
    }

    /**
     * 处理包含长度字段的消息帧
     * @param byteOrder 指定长度字段的字节序（大端序或小端序
     * @param maxFrameLength 允许的最大帧长度（以字节为单位）
     * @param lengthFieldOffset 长度字段在消息中的偏移量（从消息的起始位置开始计算）
     * @param lengthFieldLength 长度字段的长度（以字节为单位）。常见的值有 1、2、4 和 8
     * @param lengthAdjustment 长度字段值的调整量。如果长度字段表示的是整个消息的长度（包括长度字段本身），则应设置为 0；如果长度字段表示的是消息体的长度（不包括长度字段本身），则应设置为负数（通常是 -lengthFieldLength）
     * @param initialBytesToStrip 从解码后的帧中剥离的字节数。通常设置为 0，表示不剥离任何字节
     * @param failFast 如果设置为 true，解码器会在检测到帧长度超过 maxFrameLength 时立即抛出 TooLongFrameException；如果设置为 false，解码器会继续读取数据，直到读取到足够的数据来判断帧长度是否超过 maxFrameLength
     */
    public final TCPChannelManager buildLengthFieldBasedFrame(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        this.decoder = new LengthFieldBasedFrameDecoder(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
        return this;
    }

    /**
     * 自定义拆包器
     * @param decoder 自定义拆包器
     */
    public final TCPChannelManager buildCustomize(ByteToMessageDecoder decoder) {
        this.decoder = decoder;
        return this;
    }


    /**
     * 收到消息
     * @param clientIdent 客户端/从站标识
     * @param msg 收到的消息
     */
    public abstract void receivedMessage(String clientIdent, Object msg);


    /**
     * 有客户端断开连接
     * @param clientIdent 客户端/从站标识
     */
    public abstract void clientDisconnect(String clientIdent);

    /**
     * 有客户端连接
     * @param clientIdent 客户端/从站标识
     * @param address 127.0.0.1:8821这种
     */
    public abstract void clientConnected(String clientIdent, String address);


    /**
     * 有异常发生
     * @param clientIdent 客户端/从站标识
     * @param cause 异常
     */
    public abstract void exceptionCaught(String clientIdent, Throwable cause);


    /**
     * 获取连接对象
     * @param clientIdent 客户端/从站标识
     * @return ctx
     */
    public final ChannelHandlerContext getCtx(String clientIdent) {
        return TCPMasterListener.getCtx(clientIdent);
    }

    /**
     * 删除一个连接
     * @param clientIdent 客户端/从站标识
     */
    public final void deleteCtx(String clientIdent) {
        TCPMasterListener.deleteCtx(clientIdent);
    }

    /**
     * 写数据
     * @param clientIdent 客户端/从站标识
     * @param data 信息
     * @throws Exception 可能遇到的问题
     */
    public final void write(String clientIdent, byte[] data) throws Exception {
        getCtx(clientIdent).writeAndFlush(data);
    }

    public final ByteToMessageDecoder getDecoder() {
        return decoder;
    }
}
