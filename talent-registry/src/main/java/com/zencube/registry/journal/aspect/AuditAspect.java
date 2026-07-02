package com.zencube.registry.journal.aspect;

import com.zencube.registry.journal.annotation.Audited;
import com.zencube.registry.journal.entity.JournalAction;
import com.zencube.registry.journal.exception.AuditException;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.journal.util.ReflectionUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final EntityManager entityManager;

    /**
     * Intercepts methods annotated with @Audited.
     * Uses @Around to capture state before and after business logic execution.
     */
    @Around("@annotation(audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        if (audited.action() == JournalAction.UPDATE) {
            return handleUpdate(joinPoint, audited);
        } else if (audited.action() == JournalAction.CREATE) {
            return handleCreate(joinPoint, audited);
        } else if (audited.action() == JournalAction.DELETE) {
            return handleDelete(joinPoint, audited);
        }
        return joinPoint.proceed();
    }

    private Object handleCreate(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Object result = joinPoint.proceed(); // Business Logic
        
        // Try to extract ID from the returned entity or arguments
        Long entityId = ReflectionUtils.extractIdFromArgumentsOrResult(joinPoint, result, audited.idParam());
        if (entityId != null) {
            auditService.recordCreate(audited.entityType(), entityId);
        }
        return result;
    }

    private Object handleUpdate(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Long entityId = ReflectionUtils.extractIdFromArgumentsOrResult(joinPoint, null, audited.idParam());
        if (entityId == null || audited.entityClass() == Object.class) {
            log.warn("Cannot perform UPDATE audit without a resolvable ID and entityClass. Skipping audit.");
            return joinPoint.proceed();
        }

        // 1. Fetch current state from DB
        Object attachedEntity = entityManager.find(audited.entityClass(), entityId);
        if (attachedEntity == null) {
            return joinPoint.proceed(); // If not found, skip audit or let business logic fail
        }

        // 2. Clone the entity to snapshot the "Before" state
        Object snapshot = ReflectionUtils.deepClone(attachedEntity);

        // 3. Execute business logic (which modifies the attached entity in the persistence context)
        Object result = joinPoint.proceed();

        // 4. Compare cloned snapshot against the modified attached entity
        auditService.recordUpdate(audited.entityType(), entityId, snapshot, attachedEntity);

        return result;
    }

    private Object handleDelete(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Long entityId = ReflectionUtils.extractIdFromArgumentsOrResult(joinPoint, null, audited.idParam());
        Object result = joinPoint.proceed(); // Perform deletion
        
        if (entityId != null) {
            auditService.recordDelete(audited.entityType(), entityId);
        }
        return result;
    }
}
