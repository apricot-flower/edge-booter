package on.edge.server.web;

import java.lang.reflect.Method;

public class ControllerMethod {

    private Object clazz;

    private Method method;

    private HttpMethod httpMethod;

    private String uri;


    public ControllerMethod() {
    }

    public ControllerMethod(Object clazz, Method method, HttpMethod httpMethod, String uri) {
        this.clazz = clazz;
        this.method = method;
        this.httpMethod = httpMethod;
        this.uri = uri;
    }


    public Object getClazz() {
        return clazz;
    }

    public void setClazz(Object clazz) {
        this.clazz = clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
