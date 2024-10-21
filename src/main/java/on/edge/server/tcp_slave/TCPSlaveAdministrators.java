package on.edge.server.tcp_slave;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import on.edge.config.ServerItems;
import on.edge.server.ServerContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 单个tcp客户端服务器
 */
@SuppressWarnings("all")
public class TCPSlaveAdministrators implements ServerContext {

    public ScheduledExecutorService executor;


    private String name;
    private String host;
    private int port;
    private int timeout;
    private boolean reconnect;

    private Bootstrap bootstrap;

    //客户端的NIO线程组
    private EventLoopGroup group;

    private TCPSlaveChannelManager tcpSlaveChannelManager;

    public TCPSlaveAdministrators(ServerItems serverItem, TCPSlaveChannelManager tcpSlaveChannelManager) {
        this.group = new NioEventLoopGroup(5);
        this.executor = Executors.newScheduledThreadPool(3);
        this.name = serverItem.getName();
        this.host = serverItem.getHost();
        this.port = serverItem.getPort();
        this.timeout = serverItem.getTimeout() <= 0 ? 3 : serverItem.getTimeout();
        this.reconnect = serverItem.isReconnect();
        this.bootstrap = new Bootstrap();
    }

    /**
     * 启动
     */
    @Override
    public TCPSlaveAdministrators start() throws Exception {
        CompletableFuture.runAsync(() -> {
            try {
                connect(false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(ex -> {handleException(ex);
            return null;});
        if (reconnect) {
            this.tcpSlaveChannelManager = tcpSlaveChannelManager;
            executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (!TCPSlaveListener.linkedFlag(name)) {
                        connect(true);
                    }
                }
            }, timeout * 2, 10, TimeUnit.SECONDS);
        }
        return this;
    }



    private void connect(boolean flag) {
        try {
            if (flag) {
                this.group.shutdownGracefully();
                this.group = new NioEventLoopGroup(5);
                this.bootstrap = new Bootstrap();
            }
            TCPSlaveListener.linked(name, true);
            bootstrap.group(group).channel(NioSocketChannel.class)
                    //开启长连接
                    .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.timeout * 1000)
                    .option(ChannelOption.SO_SNDBUF, 10240)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 添加编码解码器
                            if (tcpSlaveChannelManager != null) {
                                ch.pipeline().addLast(tcpSlaveChannelManager.getDecoder().build());
                            }
                            ch.pipeline().addLast(new TCPSlaveBusinessHandler(tcpSlaveChannelManager, name, host, port, timeout, reconnect));
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            TCPSlaveListener.linked(name, false);
        }
    }

    /**
     * 关闭
     */
    @Override
    public void close() throws Exception {
        group.shutdownGracefully();
    }

    @Override
    public void handleException(Throwable ex) {
        ex.printStackTrace();
        System.exit(1);
    }
}
