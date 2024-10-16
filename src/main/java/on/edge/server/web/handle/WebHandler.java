package on.edge.server.web.handle;

import io.netty.handler.codec.http.FullHttpRequest;
import on.edge.BaseHandler;
import on.edge.server.web.ControllerMethod;

import java.util.Map;

/**
 * 请求类型上层
 */
public abstract class WebHandler extends BaseHandler {


    private Map<String, ControllerMethod> comps;

    public WebHandler() {
    }

    public WebHandler(Map<String, ControllerMethod> comps) {
        this.comps = comps;
    }

    public Map<String, ControllerMethod> getComps() {
        return comps;
    }

    public Object handle(FullHttpRequest fullHttpRequest) throws Exception {return null;}




}
