package com.zencube.registry.journal.util;

import com.zencube.registry.journal.exception.AuditException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

    /**
     * Extracts an ID from method arguments or the result object.
     */
    public static Long extractIdFromArgumentsOrResult(ProceedingJoinPoint joinPoint, Object result, String idParamName) {
        // 1. Try arguments
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(idParamName) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        }

        // 2. Try DTO extraction if first arg is an object
        if (args.length > 0 && args[0] != null) {
            try {
                Field idField = args[0].getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object val = idField.get(args[0]);
                if (val instanceof Long) return (Long) val;
            } catch (Exception ignored) {}
        }

        // 3. Try result extraction
        if (result != null) {
            try {
                Method getId = result.getClass().getMethod("getId");
                Object val = getId.invoke(result);
                if (val instanceof Long) return (Long) val;
            } catch (Exception ignored) {}
            
            try {
                Field idField = result.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object val = idField.get(result);
                if (val instanceof Long) return (Long) val;
            } catch (Exception ignored) {}
        }

        return null;
    }

    /**
     * Creates a shallow copy of an object using reflection to act as a snapshot.
     */
    public static Object deepClone(Object entity) {
        if (entity == null) return null;
        try {
            Class<?> clazz = entity.getClass();
            Object clone = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(clone, field.get(entity));
            }
            return clone;
        } catch (Exception e) {
            throw new AuditException("Failed to snapshot entity for audit: " + entity.getClass().getSimpleName(), e);
        }
    }
}
