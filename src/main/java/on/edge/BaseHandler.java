package on.edge;

import on.edge.server.web.ControllerMethod;
import on.edge.server.web.Param;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Parameter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings("all")
public abstract class BaseHandler {

    public <T> T cast(String valueStr, Class<T> type) {
        if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(Integer.parseInt(valueStr));
        } else if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(Long.parseLong(valueStr));
        } else if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(Double.parseDouble(valueStr));
        } else if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(Float.parseFloat(valueStr));
        } else if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(Boolean.parseBoolean(valueStr));
        } else if (type == byte.class || type == Byte.class) {
            return (T) Byte.valueOf(Byte.parseByte(valueStr));
        } else if (type == short.class || type == Short.class) {
            return (T) Short.valueOf(Short.parseShort(valueStr));
        } else if (type == char.class || type == Character.class) {
            return (T) Character.valueOf(valueStr.charAt(0));
        } else {
            return type.cast(valueStr);
        }
    }

    public Object getDefaultValue(String type) {
        switch (type) {
            case "boolean":
                return false;
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "int":
                return 0;
            case "long":
                return 0L;
            case "char":
                return '\u0000';
            case "float":
                return 0.0f;
            case "double":
                return 0.0d;
            default:
                // 引用类型，默认值为 null
                return null;
        }
    }


    public Map<String,  Class<?>> analyzeParams(ControllerMethod controllerMethod) {
        Class<?>[] parameterTypes = controllerMethod.getMethod().getParameterTypes();
        Parameter[] parameters = controllerMethod.getMethod().getParameters();
        Map<String,  Class<?>> params = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Param paramAnnotation = parameters[i].getAnnotation(Param.class);
            if (paramAnnotation != null) {
                Class<?> paramType = parameterTypes[i];
                String name = paramAnnotation.value();
                params.put(name, paramType);
            }
        }
        return params;
    }

    public boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || isWrapperType(clazz);
    }

    private boolean isWrapperType(Class<?> clazz) {
        return clazz.equals(Boolean.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Character.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Short.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Float.class);
    }

    public static List<Class<?>> getAllClassByPackageName(Package pkg) throws Exception {
        String packageName = pkg.getName();
        return getAllClassByPackageName(packageName);
    }

    public static List<Class<?>> getAllClassByPackageName(String packageName) throws Exception {
        List<Class<?>> returnClassList = getClasses(packageName);
        return returnClassList;
    }


    /**
     * 通过接口名取得某个接口下所有实现这个接口的类
     */
    public static List<Class<?>> getAllClassByInterface(Class<?> c) throws Exception {
        List<Class<?>> returnClassList = null;
        if (c.isInterface()) {
            // 获取当前的包名
            String packageName = c.getPackage().getName();
            // 获取当前包下以及子包下所以的类
            List<Class<?>> allClass = getClasses(packageName);
            if (allClass != null) {
                returnClassList = new ArrayList<Class<?>>();
                for (Class<?> cls : allClass) {
                    // 判断是否是同一个接口
                    if (c.isAssignableFrom(cls)) {
                        // 本身不加入进去
                        if (!c.equals(cls)) {
                            returnClassList.add(cls);
                        }
                    }
                }
            }
        }

        return returnClassList;
    }

    /**
     * 取得某一类所在包的所有类名 不含迭代
     */
    public static String[] getPackageAllClassName(String classLocation, String packageName) {
        // 将packageName分解
        String[] packagePathSplit = packageName.split("[.]");
        String realClassLocation = classLocation;
        int packageLength = packagePathSplit.length;
        for (int i = 0; i < packageLength; i++) {
            realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
        }
        File packeageDir = new File(realClassLocation);
        if (packeageDir.isDirectory()) {
            String[] allClassName = packeageDir.list();
            return allClassName;
        }
        return null;
    }


    private static List<Class<?>> getClasses(String packageName) throws Exception {
        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        // 循环迭代下去
        while (dirs.hasMoreElements()) {
            // 获取下一个元素
            URL url = dirs.nextElement();
            // 得到协议的名称
            String protocol = url.getProtocol();
            // 如果是以文件的形式保存在服务器上
            if ("file".equals(protocol)) {
                // 获取包的物理路径
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                // 以文件的方式扫描整个包下的文件 并添加到集合中
                findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
            } else if ("jar".equals(protocol)) {
                // 如果是jar包文件
                // 定义一个JarFile
                JarFile jar;
                // 获取jar
                jar = ((JarURLConnection) url.openConnection()).getJarFile();
                // 从此jar包 得到一个枚举类
                Enumeration<JarEntry> entries = jar.entries();
                // 同样的进行循环迭代
                while (entries.hasMoreElements()) {
                    // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    // 如果是以/开头的
                    if (name.charAt(0) == '/') {
                        // 获取后面的字符串
                        name = name.substring(1);
                    }
                    // 如果前半部分和定义的包名相同
                    if (name.startsWith(packageDirName)) {
                        int idx = name.lastIndexOf('/');
                        // 如果以"/"结尾 是一个包
                        if (idx != -1) {
                            // 获取包名 把"/"替换成"."
                            packageName = name.substring(0, idx).replace('/', '.');
                        }
                        // 如果可以迭代下去 并且是一个包
                        if ((idx != -1) || recursive) {
                            // 如果是一个.class文件 而且不是目录
                            if (name.endsWith(".class") && !entry.isDirectory()) {
                                // 去掉后面的".class" 获取真正的类名
                                String className = name.substring(packageName.length() + 1, name.length() - 6);
                                classes.add(Class.forName(packageName + '.' + className));
                            }
                        }
                    }
                }
            }
        }
        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes) throws Exception {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirFiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirFiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                // 添加到集合中去
                classes.add(Class.forName(packageName + '.' + className));
            }
        }
    }

}
