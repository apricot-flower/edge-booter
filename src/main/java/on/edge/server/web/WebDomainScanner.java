package on.edge.server.web;

import on.edge.except.ControllerException;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class WebDomainScanner {

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


    public WebDomainScanner scan() throws Exception {
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
                Object instance = clazz.getDeclaredConstructor().newInstance();
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
