package javax.annotation.processing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Generated {

    String comments() default "";

    String date() default "";

    String[] value();

}