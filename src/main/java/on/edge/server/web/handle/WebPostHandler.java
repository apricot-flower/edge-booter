package on.edge.server.web.handle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import on.edge.except.ControllerException;
import on.edge.server.web.ControllerMethod;

import java.util.*;

/**
 * 处理post请求
 */
public class WebPostHandler extends WebHandler {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_SPLIT = ";";
    private static final String FORM_DATA = "multipart/form-data";
    private static final String APPLICATION_JSON = "application/json";
    private static final String XFU = "application/x-www-form-urlencoded";

    public WebPostHandler(Map<String, ControllerMethod> comps) {
        super(comps);
    }

    @Override
    public Object handle(FullHttpRequest fullHttpRequest) throws Exception {
        String uri = fullHttpRequest.uri();
        if (!getComps().containsKey(uri)) {
            throw new ControllerException("no such uri:" + uri);
        }
        ControllerMethod controllerMethod = getComps().get(uri);
        String contentType = fullHttpRequest.headers().get(CONTENT_TYPE).split(CONTENT_TYPE_SPLIT)[0].toLowerCase();
        Object result = null;
        switch (contentType) {
            case FORM_DATA:
                result = formData(fullHttpRequest, controllerMethod);
                break;
            case APPLICATION_JSON:
                result = applicationJson(fullHttpRequest, controllerMethod);
                break;
            default:
                throw new ControllerException("Undeveloped content_type:" + contentType);
        }
        return result;
    }

    private Object applicationJson(FullHttpRequest fullHttpRequest, ControllerMethod controllerMethod) throws Exception {
        Map<String, Class<?>> params = analyzeParams(controllerMethod);
        Set<String> paramsSet = params.keySet();
        ByteBuf content = fullHttpRequest.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        JsonNode jsonNode = new ObjectMapper().readTree(reqContent);
        List<Object> request = new ArrayList<>();
        for (String key : paramsSet) {
            Class<?> type = params.get(key);
            Object value;
            //判断类型是否是基本类型
            if (!jsonNode.has(key)) {
                value = getDefaultValue(type.getName());
            } else {
                if (isPrimitiveOrWrapper(type)) {
                    value = cast(jsonNode.get(key).asText(), type);
                } else {
                    value = new ObjectMapper().treeToValue(jsonNode.get(key), type);
                }
            }
            request.add(value);
        }
        if (request.size() == 0) {
            return controllerMethod.getMethod().invoke(controllerMethod.getClazz());
        }
        return controllerMethod.getMethod().invoke(controllerMethod.getClazz(), request.toArray());
    }


    private Object formData(FullHttpRequest fullHttpRequest, ControllerMethod controllerMethod) throws Exception {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> httpPostData = decoder.getBodyHttpDatas();
        Map<String, String> params = new HashMap<>();
        for (InterfaceHttpData data : httpPostData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }
        return in(params, controllerMethod);
    }

    private Object in(Map<String, String> params, ControllerMethod controllerMethod) throws Exception {
        Map<String, Class<?>> map = analyzeParams(controllerMethod);
        List<Object> request = new ArrayList<>();
        map.forEach((key, clazz) -> {
            Object value;
            if (params.containsKey(key)) {
                value = cast(params.get(key), clazz);
            } else {
                value = getDefaultValue(clazz.getName());
            }
            request.add(value);
        });
        if (request.size() == 0) {
            return controllerMethod.getMethod().invoke(controllerMethod.getClazz());
        }
        return controllerMethod.getMethod().invoke(controllerMethod.getClazz(), request.toArray());
    }
}
