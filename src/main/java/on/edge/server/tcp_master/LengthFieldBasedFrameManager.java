package on.edge.server.tcp_master;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

/**
 * 处理包含长度字段的消息帧
 */
public class LengthFieldBasedFrameManager implements ChannelManager {

    private java.nio.ByteOrder byteOrder;
    private int maxFrameLength;
    private int lengthFieldOffset;
    private int lengthFieldLength;
    private int lengthAdjustment;
    private int initialBytesToStrip;
    private boolean failFast;


    public LengthFieldBasedFrameManager(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        this.byteOrder = byteOrder;
        this.maxFrameLength = maxFrameLength;
        this.lengthFieldOffset = lengthFieldOffset;
        this.lengthFieldLength = lengthFieldLength;
        this.lengthAdjustment = lengthAdjustment;
        this.initialBytesToStrip = initialBytesToStrip;
        this.failFast = failFast;
    }

    @Override
    public ByteToMessageDecoder build() throws Exception {
        return new LengthFieldBasedFrameDecoder(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }
}
