package org.context;

import java.util.HashMap;
import java.util.Map;

public class ControllerRegistry {
    private static final Map<Class<?>, Object> controllers = new HashMap<>();

    public static <T> void register(Class<T> clazz, T instance) {
        controllers.put(clazz, instance);
    }

    public static <T> T get(Class<T> clazz) {
        return clazz.cast(controllers.get(clazz));
    }
}

