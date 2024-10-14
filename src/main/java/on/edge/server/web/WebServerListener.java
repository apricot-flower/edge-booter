package on.edge.server.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import on.edge.server.ServerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * WEB服务
 */
public class WebServerListener implements ServerContext {

    private static final Logger logger = LogManager.getLogger(WebServerListener.class);

    private int port;
    //接收传入连接
    private EventLoopGroup boss;
    //在boss接受连接并将接受的连接注册给worker后，它处理接受的连接的流量
    private  EventLoopGroup work;
    private ServerBootstrap bootstrap;

    private WebRequestDecoder webRequestDecoder;

    public WebServerListener() {
    }

    public WebServerListener(int port) {
        this.port = port;
        this.boss = new NioEventLoopGroup();
        this.work = new NioEventLoopGroup();
        this.bootstrap = new ServerBootstrap();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 启动
     */
    @Override
    public void start() {
        try {
            this.bootstrap.group(boss, work).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("httpDecoder", new HttpServerCodec());
                            socketChannel.pipeline().addLast("httpAggregator", new HttpObjectAggregator(65535));
                            socketChannel.pipeline().addLast("webDecoder", webRequestDecoder);
                        }
            });
            logger.debug("web server starting, port:{}", this.port);
            ChannelFuture future = this.bootstrap.bind(new InetSocketAddress("127.0.0.1", this.port)).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("web server error:", e);
        } finally {
            this.boss.shutdownGracefully();
            this.work.shutdownGracefully();
        }
    }

    /**
     * 关闭
     */
    @Override
    public void close() {
        this.boss.shutdownGracefully();
        this.work.shutdownGracefully();
    }

    public WebServerListener build(Class<?> param) {
        String path = param.getName().substring(0, param.getName().lastIndexOf('.'));
        if (param.isAnnotationPresent(ControllerPath.class)) {
            ControllerPath controllerPath = param.getAnnotation(ControllerPath.class);
            path = controllerPath.value();
        }
        this.webRequestDecoder = new WebRequestDecoder().append(new WebDomainScanner().scan(path));
        return this;
    }
}
