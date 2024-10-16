package on.edge.server.web;


import com.fasterxml.jackson.databind.node.ObjectNode;
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
import on.edge.server.web.handle.WebGetHandler;
import on.edge.server.web.handle.WebHandler;
import on.edge.server.web.handle.WebPostHandler;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@SuppressWarnings("all")
public class WebServerListener implements ServerContext {



    private int port;
    private Class<?> param;
    //接收传入连接
    private EventLoopGroup boss;
    //在boss接受连接并将接受的连接注册给worker后，它处理接受的连接的流量
    private  EventLoopGroup work;
    private ServerBootstrap bootstrap;

    private WebRequestDecoder webRequestDecoder;

    private WebDomainScanner webDomainScanner;

    private final Map<String, WebHandler> handlers;

    private GlobalExceptionHandler globalExceptionHandler;


    public WebServerListener(int port,  Class<?> param) {
        this.port = port;
        this.boss = new NioEventLoopGroup();
        this.work = new NioEventLoopGroup();
        this.bootstrap = new ServerBootstrap();
        this.param = param;
        this.handlers = Collections.synchronizedMap(new Hashtable<>());
    }


    /**
     * 启动
     */
    @Override
    public WebServerListener start() throws Exception {
        CompletableFuture.runAsync(() -> {
            try {
                connect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(ex -> {handleException(ex);
            return null;});
        return this;
    }


    private void connect() throws Exception {
        this.bootstrap.group(boss, work).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("httpDecoder", new HttpServerCodec());
                        socketChannel.pipeline().addLast("httpAggregator", new HttpObjectAggregator(65535));
                        socketChannel.pipeline().addLast("webDecoder", webRequestDecoder);
                    }
                });
        ChannelFuture future = this.bootstrap.bind(new InetSocketAddress("127.0.0.1", this.port)).sync();
        future.channel().closeFuture().sync();
    }

    /**
     * 关闭
     */
    @Override
    public void close() throws Exception {
        this.boss.shutdownGracefully();
        this.work.shutdownGracefully();
    }

    /**
     * 构建表信息
     */
    public WebServerListener build(Map<String, Object> ioc, ObjectNode allConfigs) throws Exception {
        String path = param.getPackage().getName();
        if (param.isAnnotationPresent(ControllerPath.class)) {
            ControllerPath controllerPath = param.getAnnotation(ControllerPath.class);
            path = controllerPath.value();
        }
        this.webDomainScanner = new WebDomainScanner(path).scan(ioc, allConfigs);
        //初始化get请求
        this.handlers.put(HttpMethod.GET.getMethod(), new WebGetHandler(this.webDomainScanner.getRequestGets()));
        // 初始化post请求
        this.handlers.put(HttpMethod.POST.getMethod(), new WebPostHandler(this.webDomainScanner.getRequestPosts()));
        this.webRequestDecoder = new WebRequestDecoder().append(this.handlers).buildWebGlobalErrorHandle(globalExceptionHandler);
        return this;
    }

    /**
     * 全局异常处理
     * @param handler
     */
    public WebServerListener buildWebGlobalErrorHandle(GlobalExceptionHandler handler) {
        this.globalExceptionHandler = handler;
        return this;
    }

    @Override
    public void handleException(Throwable ex) {
        ex.printStackTrace();
        System.exit(1);
    }

}
