package on.edge.server.tcp_master;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

/**
 * 解码器
 */
@ChannelHandler.Sharable
@SuppressWarnings("all")
public class DecoderHandler extends ChannelInboundHandlerAdapter {

    private TCPMasterChannelManager tcpChannelManager;

    public DecoderHandler() {
    }

    public DecoderHandler(TCPMasterChannelManager tcpChannelManager) {
        this.tcpChannelManager = tcpChannelManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.tcpChannelManager.receivedMessage(getRemoteAddress(ctx), msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        TCPMasterListener.deleteCtx(getRemoteAddress(ctx));
        this.tcpChannelManager.clientDisconnect(getRemoteAddress(ctx));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String flag = getRemoteAddress(ctx);
        TCPMasterListener.addCtx(flag, ctx);
        this.tcpChannelManager.clientConnected(flag, address(ctx));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.tcpChannelManager.exceptionCaught(getRemoteAddress(ctx), cause);
    }

    private String getRemoteAddress(ChannelHandlerContext ctx) {
        StringBuilder asciiValues = new StringBuilder();
        String address = address(ctx);
        for (char ch : address.toCharArray()) {
            int asciiValue = (int) ch;
            asciiValues.append(asciiValue).append(" ");
        }
        return asciiValues.toString().trim().replace(" ", "");
    }

    private String address(ChannelHandlerContext ctx) {
        SocketChannel channel = (SocketChannel) ctx.channel();
        return channel.remoteAddress().getHostString() + ":" + channel.remoteAddress().getPort();
    }
}
