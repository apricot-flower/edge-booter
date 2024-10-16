package on.edge.server.web.handle;

import io.netty.handler.codec.http.FullHttpRequest;
import on.edge.except.ControllerException;
import on.edge.server.web.ControllerMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处理get请求
 */
public class WebGetHandler extends WebHandler {

    private static final String GET_DEFAULT_SPLIT = "\\?";
    private static final String GET_VALUE_DEFAULT_SPLIT = "&";
    private static final String GET_VALUE_TO_VALUE_DEFAULT_SPLIT = "=";

    public WebGetHandler(Map<String, ControllerMethod> comps) {
        super(comps);
    }

    @Override
    public Object handle(FullHttpRequest fullHttpRequest) throws Exception {
        String[] parts = fullHttpRequest.uri().split(GET_DEFAULT_SPLIT, 2);
        String uri = parts[0];
        String params = parts.length == 2 ? parts[1] : null;
        if (!getComps().containsKey(uri)) {
            throw new ControllerException("no such uri:" + uri);
        }
        ControllerMethod controllerMethod = getComps().get(uri);
        Map<String, Class<?>> requestItems = analyzeParams(controllerMethod);
        if (requestItems.size() == 0) {
            return controllerMethod.getMethod().invoke(controllerMethod.getClazz());
        }
        List<Object> request = new ArrayList<>();
        Map<String, String> requestParams = Arrays.stream(params.split(GET_VALUE_DEFAULT_SPLIT))
                .map(pair -> pair.split(GET_VALUE_TO_VALUE_DEFAULT_SPLIT, 2))
                .filter(keyValue -> keyValue.length == 2)
                .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]));
        requestItems.forEach((key, type) -> {
            Object value;
            if (requestParams.containsKey(key)) {
                value = cast(requestParams.get(key), type);
            } else {
                value = getDefaultValue(type.getName());
            }
            request.add(value);
        });
        return controllerMethod.getMethod().invoke(controllerMethod.getClazz(), request.toArray());
    }
}
