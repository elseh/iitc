package iitc.triangulation.aspect;

import java.lang.annotation.*;

/**
 * @author epavlova
 * @version 06.06.2016
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasValues {
}
