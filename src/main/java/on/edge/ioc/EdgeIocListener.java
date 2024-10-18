package on.edge.ioc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import on.edge.BaseHandler;
import on.edge.config.JDBCConfig;
import on.edge.except.ConfigException;
import on.edge.except.IOCException;
import on.edge.jdbc.EdgeJDBCListener;
import on.edge.jdbc.JDBCManager;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IOC扫描器
 */
@SuppressWarnings("all")
public class EdgeIocListener extends BaseHandler {

    private final Package path;

    private final String applicationName;

    private IOCAss iocAss;

    private final Map<String, Object> ioc;

    public EdgeIocListener(Class<?> param, ObjectNode allConfigs) throws Exception {
        this.path = param.getPackage();
        this.applicationName = param.getName();
        this.iocAss = new IOCAss(allConfigs);
        this.ioc = Collections.synchronizedMap(new HashMap<>());
    }

    public EdgeIocListener build() throws Exception {
        //对classes进行分类
        Map<String, Class<?>> classes = scan();
        Map<String, Object> preload = this.iocAss.ass(classes).getPreload();
        this.ioc.putAll(preload);
        return this;
    }

    public EdgeIocListener buildORM(JDBCConfig jdbcConfig) throws Exception {
        if (jdbcConfig == null) {
            return this;
        }
        String location = jdbcConfig.getLocation();
        if (location == null || location.equals("")) {
            return this;
        }
        Path path = Paths.get(location);
        if (!Files.exists(path)) {
            throw new ConfigException("JDBC location not exist!");
        }
        if (!Files.isDirectory(path)) {
            throw new ConfigException("JDBC location, It's not a folder!");
        }
        EdgeJDBCListener edgeJDBCListener = new EdgeJDBCListener(jdbcConfig);
        File[] files = scanJDBCXML(location);
        for (File file : files) {
            JDBCManager manager = new JDBCManager().build(file, edgeJDBCListener);
            String managerName = manager.getName().trim();
            if (this.ioc.containsKey(managerName)) {
                throw new IOCException("jdbc manager will add IOC but IOC has this bean name:" + managerName);
            }
            this.ioc.put(managerName, manager);
        }
        return this;
    }

    private File[] scanJDBCXML(String path) throws Exception {
        File directory = new File(path);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // 使用正则表达式检查文件名是否以"edge_"开头并且以".xml"结尾
                return name.matches("^edge_.*\\.xml$");
            }
        };
        return directory.listFiles(filter);
    }


    public Map<String, Class<?>> scan() throws Exception {
        List<Class<?>> classList = getAllClassByPackageName(this.path);
        Map<String, Class<?>> classes = new HashMap<>();
        for (Class<?> clazz : classList) {
            if (clazz.getName().equals(this.applicationName)) {
                continue;
            }
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
        return classes;
    }

    public Map<String, Object> getIoc() {
        return ioc;
    }
}
