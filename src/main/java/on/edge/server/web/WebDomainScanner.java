package on.edge.server.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 扫描controller
 */
public class WebDomainScanner {

    private static final Logger logger = LogManager.getLogger(WebDomainScanner.class);

    private final List<Class<?>> classes;

    private final Map<String, ControllerMethod> methods;

    public WebDomainScanner() {
        this.classes = Collections.synchronizedList(new ArrayList<>());
        this.methods = Collections.synchronizedMap(new HashMap<>());
    }

    public WebDomainScanner scan(String path) {
        scanController(path);
        //挨个解析controller
        try {
            for (Class<?> clazz : this.classes) {
                if (!clazz.isAnnotationPresent(Controller.class)) {
                    continue;
                }
                Controller controller = clazz.getAnnotation(Controller.class);
                String uri = controller.value();
                //扫描类下面的具体接口
                Method[] methods = clazz.getDeclaredMethods();
                if (methods.length > 0) {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    for (Method method : methods) {
                        if (Modifier.isPublic(method.getModifiers()) && method.isAnnotationPresent(Mapping.class)) {
                            Mapping mapping = method.getAnnotation(Mapping.class);
                            String methodUri = "";
                            if (mapping.uri().trim().equals("/")) {
                                methodUri = uri;
                            } else {
                                methodUri = mapping.uri();
                                if (methodUri.startsWith("/")) {
                                    methodUri = methodUri.substring(1);
                                }
                                methodUri = uri + methodUri;
                            }
                            ControllerMethod controllerMethod = new ControllerMethod(instance, method, mapping.method(), methodUri);
                            if (this.methods.containsKey(methodUri)) {
                                logger.error("重复定义uri：{}", methodUri);
                                System.exit(1);
                            }
                            this.methods.put(methodUri, controllerMethod);
                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.error("analyze controller error:", e);
            System.exit(1);
        }
        return this;
    }

    private void scanController(String path) {
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
                    Class<?> clazz = null;
                    try {
                        clazz = Thread.currentThread().getContextClassLoader().loadClass(path+"."+f.getName().replace(".class",""));
                        this.classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        logger.error("controller scan error:", e);
                    }

                }
            }
        }
    }

    public ControllerMethod driver(String uri) {
        return this.methods.getOrDefault(uri, null);
    }
}
