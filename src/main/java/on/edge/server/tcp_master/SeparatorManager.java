package on.edge.server.tcp_master;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * 基于分隔符处理数据帧
 */
public class SeparatorManager implements ChannelManager {

    private ByteBuf buf;
    private int maxFrameLength;

    public SeparatorManager(int maxFrameLength, ByteBuf buf) {
        this.buf = buf;
        this.maxFrameLength = maxFrameLength;
    }

    @Override
    public ByteToMessageDecoder build() throws Exception {
        return new DelimiterBasedFrameDecoder(maxFrameLength, buf);
    }
}
