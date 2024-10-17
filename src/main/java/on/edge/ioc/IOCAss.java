package on.edge.ioc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import on.edge.BaseHandler;
import on.edge.except.ConfigException;
import on.edge.except.IOCException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 协助分类
 */
@SuppressWarnings("all")
public class IOCAss extends BaseHandler {

    private ObjectNode allConfigs;

    private Map<String, Class<?>> classes;

    /**
     * 预加载的对象
     */
    private Map<String, Object> preload;


    public IOCAss(ObjectNode allConfigs) {
        this.allConfigs = allConfigs;
        this.preload = Collections.synchronizedMap(new HashMap<>());
    }

    public IOCAss ass(Map<String, Class<?>> classes) throws Exception {
        this.classes = classes;
        beanHandle();
        composite();

        return this;
    }

    //处理复合存在@Resource
    private void composite() throws Exception {
        Set<String> nameSet = this.classes.keySet();
        for (String name : nameSet) {
            if (this.preload.containsKey(name)) {
                continue;
            }
            preloadDemand(this.classes.get(name), name);

        }
    }

    // resourceName要加载的，upperLevelName上层名称
    private void preloadDemand(Class<?> clazz, String name) throws Exception {
        Object instance = valueHandle(clazz, name);
        Field[] fields = clazz.getDeclaredFields();
        //先处理bean
        AnnotationBeanHandle(instance, clazz, name);
        for (Field field : fields) {
            if (field.isAnnotationPresent(Resource.class)) {
                String resourceName = field.getAnnotation(Resource.class).name().trim();
                if (resourceName.equals("")) {
                    resourceName = field.getType().getName();
                }
                boolean isNamePrivate = Modifier.isPrivate(field.getModifiers());
                if (isNamePrivate) {
                    field.setAccessible(true);
                }
                if (!this.preload.containsKey(resourceName)) {
                    if (!this.classes.containsKey(resourceName)) {
                        throw new IOCException("no such ioc bean:" + resourceName);
                    }
                    preloadDemand(this.classes.get(resourceName), resourceName);
                }
                field.set(instance, this.preload.get(resourceName));
                this.preload.put(name, instance);
            }
        }



    }

    //处理没有Resource的类
    private void beanHandle() throws Exception {
        Set<String> nameSet = this.classes.keySet();
        outer:
        for (String name : nameSet) {
            Class<?> clazz = this.classes.get(name);
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Resource.class)) {
                    continue outer;
                }
            }
            Object instance = valueHandle(clazz, name);
            //处理@Bean
            AnnotationBeanHandle(instance, clazz, name);
            this.addPreload(name, clazz);
        }
    }

    public void AnnotationBeanHandle(Object instance, Class<?> clazz, String name) throws Exception {
        //处理@Bean
        Method[] publicMethods = clazz.getMethods();
        for (Method method : publicMethods) {
            if (method.isAnnotationPresent(Bean.class)) {
                String beanName = method.getAnnotation(Bean.class).name().trim();
                //获取返回值，如果方法是无参的才会执行
                if (method.getParameterCount() != 0) {
                    throw new IOCException(name + "@Bean Currently, methods with parameters are not supported!");
                }
                Object result = method.invoke(instance);
                if (beanName.equals("")) {
                    beanName = result.getClass().getName();
                }
                this.addPreload(beanName, result);
            }
        }
    }


    private Object valueHandle(Class<?> clazz, String name) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();
        //处理@Value
        for (Field field : fields) {
            if (field.isAnnotationPresent(Value.class)) {
                boolean isNamePrivate = Modifier.isPrivate(field.getModifiers());
                if (isNamePrivate) {
                    field.setAccessible(true);
                }
                Object value = analyzeConfig(field, name);
                field.set(instance, value);
            }
        }
        return instance;
    }


    private void addPreload(String name, Object data) {
        if (this.preload.containsKey(name)) {
            throw new IOCException("ioc preload run into same name:" + name);
        }
        this.preload.put(name, data);
    }



    private Object analyzeConfig(Field field, String className) throws Exception {
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

    public Map<String, Object> getPreload() {
        return preload;
    }
}
