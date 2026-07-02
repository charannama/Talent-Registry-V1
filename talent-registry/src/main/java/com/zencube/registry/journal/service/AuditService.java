package com.zencube.registry.journal.service;

public interface AuditService {
    
    /**
     * Records the creation of a new entity.
     */
    void recordCreate(String entityType, Long entityId);

    /**
     * Records the update of an entity by comparing before and after states.
     */
    void recordUpdate(String entityType, Long entityId, Object beforeEntity, Object afterEntity);

    /**
     * Records the deletion of an entity.
     */
    void recordDelete(String entityType, Long entityId);

    /**
     * Records a custom system or domain event.
     */
    void recordCustomEvent(String eventType, String resourceType, String resourceId, String details);
}
