package com.github.standobyte.jojo.util.mc.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }
    
    static <T, E> void setFieldValue(Field field, E instance, @Nullable final T value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
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
    
    // primitive equivalents for Field#get and Field#set

    static <E> int getIntFieldValue(Field field, E instance) {
        try {
            return field.getInt(instance);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }

    static <E> long getLongFieldValue(Field field, E instance) {
        try {
            return field.getLong(instance);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }

    static <E> boolean getBooleanFieldValue(Field field, E instance) {
        try {
            return field.getBoolean(instance);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }

    public static <E> float getFloatFieldValue(Field field, E instance) {
        try {
            return field.getFloat(instance);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }

    static <E> double getDoubleFieldValue(Field field, E instance) {
        try {
            return field.getDouble(instance);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }

    static <E> byte getByteFieldValue(Field field, E instance) {
        try {
            return field.getByte(instance);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }

    static <E> char getCharFieldValue(Field field, E instance) {
        try {
            return field.getChar(instance);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }
    

    
    static <E> void setIntFieldValue(Field field, E instance, int value) {
        try {
            field.setInt(instance, value);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }
    
    static <E> void setLongFieldValue(Field field, E instance, long value) {
        try {
            field.setLong(instance, value);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }
    
    static <E> void setBooleanFieldValue(Field field, E instance, boolean value) {
        try {
            field.setBoolean(instance, value);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }
    
    static <E> void setFloatFieldValue(Field field, E instance, float value) {
        try {
            field.setFloat(instance, value);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }
    
    static <E> void setDoubleFieldValue(Field field, E instance, double value) {
        try {
            field.setDouble(instance, value);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }
    
    static <E> void setByteFieldValue(Field field, E instance, byte value) {
        try {
            field.setByte(instance, value);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }
    
    static <E> void setCharFieldValue(Field field, E instance, char value) {
        try {
            field.setChar(instance, value);
        } catch (IllegalAccessException e) {
            throw UnableToAccessFieldException.onIllegalAccessException(e, field, instance);
        }
    }
    
    //

    public static class UnableToAccessFieldException extends RuntimeException {
        
        static UnableToAccessFieldException onIllegalAccessException(IllegalAccessException e, Field field, Object instance) {
            LOGGER.error(REFLECTION, "Unable to access field {} on an object of type {}", field.getName(), instance.getClass().getName(), e);
            e.printStackTrace();
            return new UnableToAccessFieldException(e);
        }
        
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
    
    
    
    public static List<Field> getFieldsIncludingSuperclasses(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            fields.addAll(getFieldsIncludingSuperclasses(superclass));
        }
        return fields;
    }
}
