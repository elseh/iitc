package iitc.triangulation.aspect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * @author epavlova
 * @version 06.06.2016
 */
public class ValueInjector {
    private Properties properties = new Properties();
    public static ValueInjector INSTANCE = new ValueInjector();
    private static Logger log = LogManager.getLogger(ValueInjector.class);

    static {
        try {
            Path path = FileSystems.getDefault().getPath("tri.properties");
            log.info("Path to load: {}" ,path.toAbsolutePath());
            INSTANCE.properties.load(Files.newBufferedReader(path));
        } catch (IOException e) {
            log.error("failed to load properties", e);
        }
    }

    public void injectStatic(Class clazz) {
        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Value.class))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .forEach(field -> inject(null, field));
    }

    public void inject(Object toInject) {
        Stream.of(toInject.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Value.class))
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .forEach(field -> inject(toInject, field));
    }

    private void inject(Object toInject, Field field) {
        field.setAccessible(true);
        Value annotation = field.getAnnotation(Value.class);
        String[] split = annotation.value().split(":");
        String key = split[0];
        String value = properties.getProperty(key, split.length > 1 ? split[1] : "");
        log.debug("Injecting: {} : '{}'", key, value);
        Object[] values = matchValues(new Class[]{field.getType()}, new String[]{value});
        try {
            if (field.getType() == int.class) {
                field.setInt(toInject, (int)values[0]);
            } else {
                field.set(toInject, values[0]);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object[] matchValues(Class[] types, String[] strings) {
        Object[] result = new Object[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String value = strings[i].trim();
            if (types[i] == int.class) {
                result[i] = Integer.parseInt(value);
            }
            if (types[i] == String.class) {
                result[i] = value;
            }
            if (Profile.class.isAssignableFrom(types[i])) {
                result[i] = calculateProfile((Class<? extends Profile>) types[i], value);
            }
        }
        return result;
    }

    private Profile calculateProfile(Class<? extends Profile> fieldType , String value) {
        try {
            Field valuesField = fieldType.getDeclaredField("values");
            valuesField.setAccessible(true);
            Profile[] values = (Profile[]) valuesField.get(null);
            for (Profile val : values) {
                if (Objects.equals(val.getName(), value)) {
                    return val;
                }
            }
            String[] paramStrings = value.split(",");
            Constructor<? extends Profile> constructor = (Constructor<? extends Profile>) Stream.of(fieldType.getDeclaredConstructors())
                    .filter(c -> c.getGenericParameterTypes().length == paramStrings.length)
                    .findFirst().get();
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] paramValues = matchValues(paramTypes, paramStrings);
            return constructor.newInstance(paramValues);
        } catch (NoSuchFieldException e) {
            log.error("Profile should have static 'values' field", e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
