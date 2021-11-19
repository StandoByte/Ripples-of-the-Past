package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class ReflectionUtil {
    static final Logger LOGGER = LogManager.getLogger();
    static final Marker REFLECTION = MarkerManager.getMarker("REFLECTION");

    @Nullable
    static <T, E> T getFieldValue(Field field, E instance) {
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            LOGGER.error(REFLECTION, "Unable to access field {} on an object of type {}", field.getName(), instance.getClass().getName(), e);
            e.printStackTrace();
            throw new UnableToAccessFieldException(e);
        }
    }
    
    static <T, E> void setFieldValue(Field field, T instance, @Nullable final E value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            LOGGER.error(REFLECTION, "Unable to access field {} on an object of type {}", field.getName(), instance.getClass().getName(), e);
            e.printStackTrace();
            throw new UnableToAccessFieldException(e);
        }
    }
    
    static <T, E> T invokeMethod(Method method, E instance, Object... args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            LOGGER.error(REFLECTION, "Unable to access method {} on an object of type {}", method.getName(), instance.getClass().getName(), e);
            e.printStackTrace();
            throw new UnableToAccessMethodException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new ReflectivelyAccessedMethodException(e.getCause());
        }
    }

    public static class UnableToAccessFieldException extends RuntimeException {
        private UnableToAccessFieldException(IllegalAccessException e) {
            super(e);
        }
    }

    public static class UnableToAccessMethodException extends RuntimeException {
        private UnableToAccessMethodException(IllegalAccessException e) {
            super(e);
        }
    }

    public static class ReflectivelyAccessedMethodException extends RuntimeException {
        private ReflectivelyAccessedMethodException(Throwable e) {
            super(e);
        }
    }
}
