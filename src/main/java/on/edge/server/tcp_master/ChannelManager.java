package on.edge.server.tcp_master;

import io.netty.handler.codec.ByteToMessageDecoder;

public interface ChannelManager {

    ByteToMessageDecoder build() throws Exception;
}
