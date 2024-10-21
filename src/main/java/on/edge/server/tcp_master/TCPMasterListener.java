package on.edge.server.tcp_master;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import on.edge.server.ServerContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * TCP主站
 */
@SuppressWarnings("all")
public class TCPMasterListener implements ServerContext {

    private static final Map<String, ChannelHandlerContext> masterCtxMap = Collections.synchronizedMap(new HashMap<>());

    private int port;

    private ChannelManager decoder;

    private EventLoopGroup boss;
    //在boss接受连接并将接受的连接注册给worker后，它处理接受的连接的流量
    private  EventLoopGroup work;
    private ServerBootstrap bootstrap;

    private DecoderHandler decoderHandler;

    private TCPMasterChannelManager tcpChannelManager;

    public TCPMasterListener(int port, TCPMasterChannelManager tcpChannelManager) {
        this.port = port;
        this.tcpChannelManager = tcpChannelManager;
        if (tcpChannelManager == null) {
            this.decoderHandler = new DecoderHandler();
        } else {
            this.decoder = tcpChannelManager.getDecoder();
            this.decoderHandler = new DecoderHandler(tcpChannelManager);
        }
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
        if (this.tcpChannelManager != null) {
            this.tcpChannelManager.run();
        }
        return this;
    }


    public void open() throws Exception {
        this.bootstrap.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        if (decoder != null) {
                            ch.pipeline().addLast(decoder.build());
                        }
                        ch.pipeline().addLast(decoderHandler);
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


    /**
     * 获取连接
     */
    public static ChannelHandlerContext getCtx(String ident) {
        return masterCtxMap.getOrDefault(ident, null);
    }

    /**
     * 添加连接
     */
    public static void addCtx(String ident, ChannelHandlerContext ctx) {
        masterCtxMap.put(ident, ctx);
    }

    /**
     * 删除一个连接
     */
    public static void deleteCtx(String ident) {
        masterCtxMap.remove(ident);
    }

}
