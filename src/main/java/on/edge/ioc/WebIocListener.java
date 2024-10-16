package on.edge.ioc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import on.edge.BaseHandler;
import on.edge.except.ConfigException;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * IOC扫描器
 */
@SuppressWarnings("all")
public class WebIocListener extends BaseHandler {

    private final String path;

    private ObjectNode allConfigs;

    private final Map<String, Class<?>> classes;

    private final Map<String, Object> ioc;


    public WebIocListener(Class<?> param, ObjectNode allConfigs) {
        this.path = param.getName().substring(0, param.getName().lastIndexOf('.'));
        this.classes = new HashMap<>();
        this.allConfigs = allConfigs;
        this.ioc = Collections.synchronizedMap(new HashMap<>());
    }

    // todo controller中没有注入
    public WebIocListener build() throws Exception {
        scan(path);
        Set<String> keySet = this.classes.keySet();
        for (String name : keySet) {
            Class<?> clazz = this.classes.get(name);
            //监测是否存在@Value
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                String modifierString = java.lang.reflect.Modifier.toString(modifiers);
                // 获取字段的类型和名称
                String fieldName = field.getName();

                Object value;
                // todo  打印字段信息
                System.out.println(modifierString + " " + field.getType().getName() + " " + fieldName);
                // todo 开始处理
                if (field.isAnnotationPresent(Value.class)) {
                    //解析配置文件
                    value = analyzeConfig(field, name);
                } else if (field.isAnnotationPresent(Resource.class)) {
                    //解析类
                } else {
                    continue;
                }
            }
        }
        return this;
    }

    private Object analyzeConfig(Field field, String className) throws Exception {
        String typeName = field.getType().getName();
        String valuePath = field.getAnnotation(Value.class).name().trim();
        if (valuePath.equals("")) {
            throw new ConfigException(className + " " + field.getName() + "@Value(" + valuePath + " not found!");
        }
        String[] pathParams = valuePath.split("\\.");
        JsonNode config = null;
        for (String path : pathParams) {
            config = config == null ? this.allConfigs.get(path) : config.get(path);
            if (config == null) {
                throw new ConfigException(className + " " + field.getName() + "@Value(" + valuePath + " not found!");
            }
        }
        String configValue = config.asText();
        return cast(configValue, field.getType());
    }




    public void scan(String path) throws Exception {
        String fileName = path.replaceAll("\\.", "/");
        File file = new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(fileName)).getFile());
        File[] files = file.listFiles();
        assert files != null;
        for (File f : files) {
            if (f.isDirectory()) {
                String currentPathName = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(File.separator)+1);
                scan(path+"."+currentPathName);
            } else {
                if (f.getName().endsWith(".class")) {
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(path+"."+f.getName().replace(".class",""));
                    if (!clazz.isAnnotationPresent(Component.class)) {
                        continue;
                    }
                    String clazzName = clazz.getAnnotation(Component.class).name();
                    clazzName = clazzName.trim().equals("") ? clazz.getName() : clazzName;
                    if (this.classes.containsKey(clazzName)) {
                        throw new ConfigException("There is a loading class with the same name:" + clazzName);
                    } else {
                        this.classes.put(clazzName, clazz);
                    }
                }
            }
        }
    }
}
