package on.edge.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入类
 */
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时有效
@Target(ElementType.FIELD)
public @interface Resource {
    String name() default "";
}
