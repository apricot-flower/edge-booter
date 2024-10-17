package on.edge.ioc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import on.edge.BaseHandler;
import on.edge.except.ConfigException;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * IOC扫描器
 */
@SuppressWarnings("all")
public class EdgeIocListener extends BaseHandler {

    private final String path;

    private IOCAss iocAss;

    private final Map<String, Object> ioc;


    public EdgeIocListener(Class<?> param, ObjectNode allConfigs) {
        this.path = param.getName().substring(0, param.getName().lastIndexOf('.'));
        this.iocAss = new IOCAss(allConfigs);
        this.ioc = Collections.synchronizedMap(new HashMap<>());
    }

    public EdgeIocListener build() throws Exception {
        Map<String, Class<?>> classes = new HashMap<>();
        scan(path, classes);
        if (classes.size() == 0) {
            return this;
        }
        //对classes进行分类
        Map<String, Object> preload = this.iocAss.ass(classes).getPreload();
        this.ioc.putAll(preload);
        return this;
    }


    public void scan(String path, Map<String, Class<?>> classes) throws Exception {
        String fileName = path.replaceAll("\\.", "/");
        File file = new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(fileName)).getFile());
        File[] files = file.listFiles();
        assert files != null;
        for (File f : files) {
            if (f.isDirectory()) {
                String currentPathName = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(File.separator)+1);
                scan(path+"."+currentPathName, classes);
            } else {
                if (f.getName().endsWith(".class")) {
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(path+"."+f.getName().replace(".class",""));
                    if (!clazz.isAnnotationPresent(Component.class)) {
                        continue;
                    }
                    String clazzName = clazz.getAnnotation(Component.class).name();
                    clazzName = clazzName.trim().equals("") ? clazz.getName() : clazzName;
                    if (classes.containsKey(clazzName)) {
                        throw new ConfigException("There is a loading class with the same name:" + clazzName);
                    } else {
                        classes.put(clazzName, clazz);
                    }
                }
            }
        }
    }

    public Map<String, Object> getIoc() {
        return ioc;
    }
}
