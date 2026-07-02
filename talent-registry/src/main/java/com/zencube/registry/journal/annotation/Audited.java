package com.zencube.registry.journal.annotation;

import com.zencube.registry.journal.entity.JournalAction;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {
    
    /**
     * The type of action being performed (CREATE, UPDATE, DELETE).
     */
    JournalAction action();

    /**
     * The logical string representation of the entity (e.g., "ENTERPRISE", "OPENING").
     */
    String entityType();

    /**
     * The Class of the JPA Entity being audited. Required for UPDATE/DELETE to fetch snapshots.
     */
    Class<?> entityClass() default Object.class;

    /**
     * The SpEL expression or parameter name to extract the entity ID. 
     * Default expects a parameter named 'id'.
     */
    String idParam() default "id";
}
