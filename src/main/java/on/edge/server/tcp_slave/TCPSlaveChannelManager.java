package on.edge.server.tcp_slave;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import on.edge.server.tcp_master.ChannelManager;
import on.edge.server.tcp_master.FixedLengthManager;
import on.edge.server.tcp_master.LengthFieldBasedFrameManager;
import on.edge.server.tcp_master.SeparatorManager;

import java.nio.ByteOrder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 编解码器
 */
public abstract class TCPSlaveChannelManager {

    private ChannelManager decoder;

    private final Lock lock = new ReentrantLock();

    private ChannelHandlerContext ctx;


    /**
     * 基于分隔符处理数据帧
     * @param separator 分隔符
     * @param maxFrameLength 报文最大长度
     */
    public final TCPSlaveChannelManager buildSeparator(String separator, int maxFrameLength) {
        ByteBuf buf = Unpooled.copiedBuffer(separator.getBytes());
        this.decoder = new SeparatorManager(maxFrameLength, buf);
        return this;
    }


    /**
     * 基于报文长度处理数据帧
     * @param length 报文长度
     */
    public final TCPSlaveChannelManager buildFixedLength(int length) {
        this.decoder = new FixedLengthManager(length);
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
    public final TCPSlaveChannelManager buildLengthFieldBasedFrame(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        this.decoder = new LengthFieldBasedFrameManager(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
        return this;
    }

    /**
     * 自定义拆包器
     * @param manager 自定义拆包器
     */
    public final TCPSlaveChannelManager buildCustomize(ChannelManager manager) {
        this.decoder = manager;
        return this;
    }


    ChannelManager getDecoder() {
        return decoder;
    }


    /**
     * 收到消息
     * @param msg 收到的消息
     * @param name 该连接名称
     * @param host 主站ip
     * @param port 主站端口
     */
    public abstract void receivedMessage(Object msg, String name, String host, int port);

    public void update(ChannelHandlerContext ctx) throws Exception {
        lock.lock();
        this.ctx = ctx;
        lock.unlock();
    };

    /**
     * 连接成功
     * @param name 该连接名称
     * @param host 主站ip
     * @param port 主站端口
     * @param address 本地连接的地址
     */
    public abstract void connected(String name, String host, int port, String address);

    /**
     * 断开连接
     * @param name 该连接名称
     * @param host 主站ip
     * @param port 主站端口
     * @param address 本地连接的地址
     * @param reconnect 是否配置了断线重连
     */
    public abstract void disConnected(String name, String host, int port, String address, boolean reconnect);

    /**
     * 发生异常
     * @param cause 异常
     * @param name 该连接名称
     * @param host 主站ip
     * @param port 主站端口
     * @param address 本地连接的地址
     */
    public abstract void exceptionCaught(Throwable cause, String name, String host, int port, String address);

    /**
     * 写数据
     */
    public final void write(byte[] msg) {
        lock.lock();
        this.ctx.writeAndFlush(msg);
        lock.unlock();
    }

    /**
     * 业务方法
     */
    public abstract void run();
}
