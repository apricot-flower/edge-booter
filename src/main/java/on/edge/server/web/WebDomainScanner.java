package on.edge.server.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import on.edge.BaseHandler;
import on.edge.except.ConfigException;
import on.edge.except.ControllerException;
import on.edge.except.IOCException;
import on.edge.ioc.Resource;
import on.edge.ioc.Value;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings("all")
public class WebDomainScanner extends BaseHandler {

    private final List<Class<?>> classes;

    private final Map<String, ControllerMethod> requestGets;

    private final Map<String, ControllerMethod> requestPosts;

    private final String path;


    public WebDomainScanner(String path) {
        this.path = path;
        this.classes = Collections.synchronizedList(new ArrayList<>());
        this.requestGets = Collections.synchronizedMap(new HashMap<>());
        this.requestPosts = Collections.synchronizedMap(new HashMap<>());
    }


    public WebDomainScanner scan(Map<String, Object> ioc, ObjectNode allConfigs) throws Exception {
        scanController(path);
        List<String> uriList = new ArrayList<>();
        //挨个解析controller
        for (Class<?> clazz : this.classes) {
            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }
            Controller controller = clazz.getAnnotation(Controller.class);
            String uri = controller.value().replaceAll("/", "").trim();
            //扫描类下面的具体接口
            Method[] methods = clazz.getDeclaredMethods();
            if (methods.length > 0) {
                Object instance = valueHandle(clazz, clazz.getName(), ioc, allConfigs);
                for (Method method : methods) {
                    if (Modifier.isPublic(method.getModifiers()) && method.isAnnotationPresent(Mapping.class)) {
                        Mapping mapping = method.getAnnotation(Mapping.class);
                        String[] methodUriSplit = mapping.uri().split("/");
                        String methodUri = ("/" + uri + String.join("/", methodUriSplit)).replace("//", "/");
                        ControllerMethod controllerMethod = new ControllerMethod(instance, method, mapping.method(), methodUri);
                        if (uriList.contains(methodUri)) {
                            throw new ControllerException("Repeat URI:" + methodUri);
                        } else {
                            uriList.add(methodUri);
                        }
                        if (mapping.method() == HttpMethod.GET) {
                            //get请求
                            this.requestGets.put(methodUri, controllerMethod);
                        } else if (mapping.method() == HttpMethod.POST) {
                            this.requestPosts.put(methodUri, controllerMethod);
                        }
                    }
                }
            }
        }
        return this;
    }

    private Object valueHandle(Class<?> clazz, String name, Map<String, Object> ioc, ObjectNode allConfig) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();
        //处理@Value
        for (Field field : fields) {
            if (field.isAnnotationPresent(Value.class)) {
                ifFieldPrivate(field);
                Object value = analyzeConfig(field, name, allConfig);
                field.set(instance, value);
            } else if (field.isAnnotationPresent(Resource.class)) {
                String resourceName = field.getAnnotation(Resource.class).name().trim();
                if (resourceName.equals("")) {
                    resourceName = field.getType().getName();
                }
                if (!ioc.containsKey(resourceName)) {
                    throw new IOCException("IOC error : " + name +" not found @Resource " + resourceName);
                }
                ifFieldPrivate(field);
                field.set(instance, ioc.get(resourceName));
            } else {
                continue;
            }
        }
        return instance;
    }

    private void ifFieldPrivate(Field field) {
        boolean isNamePrivate = Modifier.isPrivate(field.getModifiers());
        if (isNamePrivate) {
            field.setAccessible(true);
        }
    }

    private Object analyzeConfig(Field field, String className, ObjectNode allConfigs) throws Exception {
        String valuePath = field.getAnnotation(Value.class).name().trim();
        if (valuePath.equals("")) {
            throw new ConfigException(className + " " + field.getName() + "@Value(" + valuePath + " not found!");
        }
        String[] pathParams = valuePath.split("\\.");
        JsonNode config = null;
        for (String path : pathParams) {
            config = config == null ? allConfigs.get(path) : config.get(path);
            if (config == null) {
                throw new ConfigException(className + " " + field.getName() + "@Value(" + valuePath + " not found!");
            }
        }
        String configValue = config.asText();
        return cast(configValue, field.getType());
    }


    private void scanController(String path) throws Exception {
        String fileName = path.replaceAll("\\.", "/");
        File file = new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(fileName)).getFile());
        File[] files = file.listFiles();
        assert files != null;
        for (File f : files) {
            if (f.isDirectory()) {
                String currentPathName = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(File.separator)+1);
                scanController(path+"."+currentPathName);
            } else {
                if (f.getName().endsWith(".class")) {
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(path+"."+f.getName().replace(".class",""));
                    this.classes.add(clazz);
                }
            }
        }
    }


    public Map<String, ControllerMethod> getRequestGets() {
        return requestGets;
    }

    public Map<String, ControllerMethod> getRequestPosts() {
        return requestPosts;
    }
}
