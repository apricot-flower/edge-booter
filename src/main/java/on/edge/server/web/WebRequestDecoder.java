package on.edge.server.web;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * web服务编解码器
 */
@ChannelHandler.Sharable
public class WebRequestDecoder extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String GET_DEFAULT_SPLIT = "\\?";
    private static final String GET_VALUE_DEFAULT_SPLIT = "&";
    private static final String GET_VALUE_TO_VALUE_DEFAULT_SPLIT = "=";

    private static final Logger logger = LogManager.getLogger(WebRequestDecoder.class);

    private WebDomainScanner webDomainScanner;

    public WebRequestDecoder append(WebDomainScanner webDomainScanner) {
        this.webDomainScanner = webDomainScanner;
        return this;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        //获取请求方法
        String method = fullHttpRequest.method().name();
        if (method.equals(HttpMethod.OPTIONS.getMethod())) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpHeaders headers = response.headers();
            headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS");
            headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization");
            headers.setInt(HttpHeaderNames.CONTENT_LENGTH, 0);
            channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else if (method.equals(HttpMethod.GET.getMethod())) {
            getMethod(channelHandlerContext, fullHttpRequest);
        } else if (method.equals(HttpMethod.POST.getMethod())) {
            // todo post请求
        } else {
            error(channelHandlerContext, "http method error！");
        }


    }

    //get请求
    private void getMethod(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {
        try {
            //解析uti
            String[] parts = fullHttpRequest.uri().split(GET_DEFAULT_SPLIT, 2);
            String uri = parts[0];
            ControllerMethod controllerMethod = this.webDomainScanner.driver(uri);
            if (controllerMethod == null) {
                error(channelHandlerContext, "No corresponding content was found for the request!");
                return;
            }
            Object result;
            //调用方法
            Class<?>[] parameterTypes = controllerMethod.getMethod().getParameterTypes();
            if (parts.length == 1) {
                result = controllerMethod.getMethod().invoke(controllerMethod.getClazz());
            } else if (parameterTypes.length != 0) {
                //调用方法
                Parameter[] parameters = controllerMethod.getMethod().getParameters();
                // 打印参数信息
                Map<String,  Class<?>> params = new LinkedHashMap<>();
                for (int i = 0; i < parameters.length; i++) {
                    Param paramAnnotation = parameters[i].getAnnotation(Param.class);
                    if (paramAnnotation != null) {
                        Class<?> paramType = parameterTypes[i];
                        String name = paramAnnotation.value();
                        params.put(name, paramType);
                    }
                }
                Map<String, String> requestItems = Arrays.stream(parts[1].split(GET_VALUE_DEFAULT_SPLIT))
                        .map(pair -> pair.split(GET_VALUE_TO_VALUE_DEFAULT_SPLIT, 2))
                        .filter(keyValue -> keyValue.length == 2)
                        .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]));
                List<Object> request = new ArrayList<>();
                params.forEach((key, type) -> {
                    Object value;
                    if (requestItems.containsKey(key)) {
                        value = cast(requestItems.get(key), type);
                    } else {
                        value =getDefaultValue(type.getName());
                    }
                    request.add(value);
                });
                result = controllerMethod.getMethod().invoke(controllerMethod.getClazz(), request.toArray());
            } else {
                result = controllerMethod.getMethod().invoke(controllerMethod.getClazz());
            }
            normal(channelHandlerContext, result);
        } catch (Exception e) {
            logger.error("uri_method handle err:", e);
            error(channelHandlerContext, JSONObject.toJSONString(e));
        }
    }


    private void error(ChannelHandlerContext channelHandlerContext, String meg) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY, Unpooled.wrappedBuffer(meg.getBytes()));
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void normal(ChannelHandlerContext channelHandlerContext, Object msg) {
        FullHttpResponse response;
        if (msg == null) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(JSONObject.toJSONBytes(msg)));
        }

        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    public Object getDefaultValue(String type) {
        switch (type) {
            case "boolean":
                return false;
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "int":
                return 0;
            case "long":
                return 0L;
            case "char":
                return '\u0000';
            case "float":
                return 0.0f;
            case "double":
                return 0.0d;
            default:
                // 引用类型，默认值为 null
                return null;
        }
    }

    private <T> T cast(String valueStr, Class<T> type) {
        if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(Integer.parseInt(valueStr));
        } else if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(Long.parseLong(valueStr));
        } else if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(Double.parseDouble(valueStr));
        } else if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(Float.parseFloat(valueStr));
        } else if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(Boolean.parseBoolean(valueStr));
        } else if (type == byte.class || type == Byte.class) {
            return (T) Byte.valueOf(Byte.parseByte(valueStr));
        } else if (type == short.class || type == Short.class) {
            return (T) Short.valueOf(Short.parseShort(valueStr));
        } else if (type == char.class || type == Character.class) {
            return (T) Character.valueOf(valueStr.charAt(0));
        } else {
            return type.cast(valueStr);
        }
    }
}
