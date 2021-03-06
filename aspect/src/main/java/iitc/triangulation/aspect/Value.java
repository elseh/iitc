package iitc.triangulation.aspect;

import java.lang.annotation.*;

/**
 * @author epavlova
 * @version 06.06.2016
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    String value();
}
