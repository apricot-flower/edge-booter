package on.edge.server.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import on.edge.server.web.handle.WebHandler;

import java.util.Map;

/**
 * web服务编解码器
 */
@ChannelHandler.Sharable
public class WebRequestDecoder extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final String FAVICON_ICO = "/favicon.ico";


    private Map<String, WebHandler> handlers;

    public WebRequestDecoder append(Map<String, WebHandler> handlers) {
        this.handlers = handlers;
        return this;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        //先判断是不是FAVICON_ICO
        String uri = msg.uri();
        if (uri.equalsIgnoreCase(FAVICON_ICO)) {
            return;
        }
        //判断是否是options
        String method = msg.method().name().toUpperCase();
        if (method.equals(HttpMethod.OPTIONS.getMethod())) {
            optionsHandle(ctx);
        } else if (this.handlers.containsKey(method)) {
            //处理非特殊请求
            try {
                Object result = this.handlers.get(method).handle(msg);
                FullHttpResponse response;
                if (result == null) {
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(mapper.writeValueAsBytes(result)));
                }
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } catch (Exception e) {
                e.printStackTrace();
                error(ctx, new ObjectMapper().writeValueAsString(e.getMessage()));
            }
        } else {
            //抛出异常
            error(ctx, "no such httpMethod");
        }

    }

    private void optionsHandle(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpHeaders headers = response.headers();
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS");
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization");
        headers.setInt(HttpHeaderNames.CONTENT_LENGTH, 0);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void error(ChannelHandlerContext channelHandlerContext, String meg) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY, Unpooled.wrappedBuffer(meg.getBytes()));
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }




}
