package on.edge.server.tcp_slave;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

@SuppressWarnings("all")
public class TCPSlaveBusinessHandler extends ChannelInboundHandlerAdapter {

    private TCPSlaveChannelManager tcpSlaveChannelManager;

    private String name;
    private String host;

    private int port;
    private int timeout;
    private boolean reconnect;

    public TCPSlaveBusinessHandler(TCPSlaveChannelManager tcpSlaveChannelManager, String name, String host, int port, int timeout, boolean reconnect) {
        this.tcpSlaveChannelManager = tcpSlaveChannelManager;
        this.name = name;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.reconnect = reconnect;
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (this.tcpSlaveChannelManager != null) {
            TCPSlaveListener.linked(name, false);
            this.tcpSlaveChannelManager.update(null);
            this.tcpSlaveChannelManager.disConnected(name, host, port, address(ctx), reconnect);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (this.tcpSlaveChannelManager != null) {
            this.tcpSlaveChannelManager.update(ctx);
            this.tcpSlaveChannelManager.connected(name, host, port, address(ctx));
            this.tcpSlaveChannelManager.run();
        }
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (this.tcpSlaveChannelManager != null) {
            this.tcpSlaveChannelManager.receivedMessage(msg, name, host, port);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (this.tcpSlaveChannelManager != null) {
            this.tcpSlaveChannelManager.exceptionCaught(cause, name, host, port, address(ctx));
        }

    }


    private String address(ChannelHandlerContext ctx) {
        SocketChannel channel = (SocketChannel) ctx.channel();
        return channel.localAddress().getHostString() + ":" + channel.localAddress().getPort();
    }
}
