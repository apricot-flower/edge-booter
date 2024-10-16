package on.edge;

import on.edge.server.web.ControllerMethod;
import on.edge.server.web.Param;

import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

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
}
