package on.edge.server.tcp_master;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;

/**
 * 基于报文长度处理数据帧
 */
public class FixedLengthManager implements ChannelManager {

    private int length;

    public FixedLengthManager(int length) {
        this.length = length;
    }

    @Override
    public ByteToMessageDecoder build() throws Exception {
        return new FixedLengthFrameDecoder(length);
    }
}
