package on.edge.server.tcp_master;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import on.edge.server.ServerContext;

import java.util.concurrent.CompletableFuture;

/**
 * TCP主站
 */
@SuppressWarnings("all")
public class TCPMasterListener implements ServerContext {

    private int port;

    private EventLoopGroup boss;
    //在boss接受连接并将接受的连接注册给worker后，它处理接受的连接的流量
    private  EventLoopGroup work;
    private ServerBootstrap bootstrap;

    public TCPMasterListener(int port) {
        this.port = port;
        this.boss = new NioEventLoopGroup(1);
        this.work = new NioEventLoopGroup();
        this.bootstrap = new ServerBootstrap();
    }

    /**
     * 启动
     */
    @Override
    public TCPMasterListener start() throws Exception {
        CompletableFuture.runAsync(() -> {
            try {
                open();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(ex -> {handleException(ex);
            return null;});
        return this;
    }


    public void open() throws Exception {
        this.bootstrap.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("decoder", new DecoderHandler());
                    }
                });
        ChannelFuture channelFuture = this.bootstrap.bind(this.port).sync();
        channelFuture.channel().closeFuture().sync();
    }

    /**
     * 关闭
     */
    @Override
    public void close() throws Exception {
        this.boss.shutdownGracefully();
        this.work.shutdownGracefully();
    }

    @Override
    public void handleException(Throwable ex) {
        ex.printStackTrace();
        System.exit(1);
    }

}
